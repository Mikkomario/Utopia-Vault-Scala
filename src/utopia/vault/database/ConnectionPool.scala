package utopia.vault.database

import java.time.{Duration, Instant}

import utopia.flow.util.CollectionExtensions._
import utopia.flow.util.TimeExtensions._
import utopia.flow.async.{Volatile, VolatileFlag}
import utopia.flow.collection.VolatileList
import utopia.flow.util.WaitUtils

import scala.collection.immutable.VectorBuilder
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/**
  * This pool is used for creating and reusing database connections
  * @author Mikko Hilpinen
  * @since 7.5.2019, v1.1+
  */
class ConnectionPool(maxConnections: Int, maxClientsPerConnection: Int, val connectionKeepAlive: Duration)
{
	// ATTRIBUTES	----------------------
	
	private val connections = VolatileList[ReusableConnection]()
	private val waitLock = new AnyRef()
	private val timeoutCompletion: Volatile[Future[Any]] = new Volatile(Future.successful(Unit))
	
	private val maxClientThresholds =
	{
		var currentMax = 1
		var start = 0
		
		// Uses halving algorithm to find the thresholds (for example getting 0 to 100: 50, 75, 87, 93, 96, 98, 99)
		val buffer = new VectorBuilder[(Int, Int)]()
		while (currentMax < maxClientsPerConnection - 1)
		{
			val length = (maxConnections - start) / 2
			if (length > 0)
			{
				buffer += (start + length -> currentMax)
				currentMax += 1
				start += length
			}
			else
				currentMax = maxClientsPerConnection
		}
		
		buffer += (maxConnections -> maxClientsPerConnection)
		buffer.result()
	}
	
	
	// COMPUTED	---------------------------
	
	private def connection = connections.pop
	{
		all =>
			// Returns the first reusable connection, if no such connection exists, creates a new connection
			// Tries to use the connection with least clients
			val reusable = if (all.nonEmpty) Some(all.minBy { _.currentClientCount }).filter {
				_.tryJoin(maxClientPerConnectionWhen(all.size)) } else None
			
			reusable.map { _ -> all } getOrElse
				{
					val newConnection = new ReusableConnection()
					newConnection -> (all :+ newConnection)
				}
	}
	
	
	// OPERATORS	-----------------------
	
	/**
	  * Performs an operation using a database connection. The function must not use the connection after its completion.
	  * Please use tryWith(...) if you wish to catch the (likely) exceptions thrown by the provided function
	  * @param f The function that will be run
	  * @param context Asynchronous execution context for closing open connections afterwards
	  * @tparam B Result type
	  * @return Function results
	  */
	def apply[B](f: Connection => B)(implicit context: ExecutionContext) = connection.doAndLeave(f)
	
	
	// OTHER	---------------------------
	
	/**
	  * Performs an operation using a database connection. The function must not use the connection after its completion.
	  * Catches any exceptions
	  * @param f The function that will be run
	  * @param context Asynchronous execution context for closing open connections afterwards
	  * @tparam B Result type
	  * @return Function results, wrapped in a try
	  */
	def tryWith[B](f: Connection => B)(implicit context: ExecutionContext) = connection.tryAndLeave(f)
	
	// Finds the first treshold that hasn't been reached and uses that connection amount
	private def maxClientPerConnectionWhen(openConnectionCount: Int) =
		(maxClientThresholds.find { _._1 >= openConnectionCount } getOrElse maxClientThresholds.last)._2
	
	private def closeUnusedConnections()(implicit context: ExecutionContext) =
	{
		// Makes sure connection closing is active
		timeoutCompletion.update
		{
			old =>
				// Will not create another future if one is active already
				if (old.isCompleted)
				{
					Future
					{
						var nextWait: Option[Instant] = Some(Instant.now() + connectionKeepAlive)
						
						// Closes connections as long as they are queued to be closed
						while (nextWait.isDefined)
						{
							WaitUtils.waitUntil(nextWait.get, waitLock)
							
							// Updates connection list and determines next close time
							nextWait = connections.pop
							{
								all =>
									// Keeps connections that are still open
									val (closing, open) = all.divideBy { _.isOpen }
									closing.foreach { _.tryClose() }
									val lastLeaveTime = open.filterNot { _.isInUse }.map { _.lastLeaveTime }.minOption
									
									lastLeaveTime.map { _ + connectionKeepAlive } -> open
							}
						}
					}
				}
				else
					old
		}
	}
	
	
	// NESTED CLASSES	-------------------
	
	private class ReusableConnection
	{
		// ATTRIBUTES	-------------------
		
		private val closed = new VolatileFlag()
		private val connection = new Connection()
		private val clientCount = new Volatile(1)
		
		private var _lastLeaveTime = Instant.now()
		
		
		// COMPUTED	-----------------------
		
		def lastLeaveTime = _lastLeaveTime
		
		def currentClientCount = clientCount.get
		
		def isInUse = currentClientCount > 0
		
		def isOpen = isInUse || (Instant.now < _lastLeaveTime + connectionKeepAlive)
		
		
		// OTHER	-----------------------
		
		def tryAndLeave[B](f: Connection => B)(implicit context: ExecutionContext) = Try(doAndLeave(f))
		
		def doAndLeave[B](f: Connection => B)(implicit context: ExecutionContext) =
		{
			try
			{
				f(connection)
			}
			finally
			{
				leave()
			}
		}
		
		def tryJoin(currentMaxCapacity: Int) = clientCount.pop
		{
			currentCount =>
				if (currentCount >= currentMaxCapacity || closed.isSet)
					false -> currentCount
				else
					true -> (currentCount + 1)
		}
		
		def leave()(implicit context: ExecutionContext) =
		{
			_lastLeaveTime = Instant.now()
			clientCount.update
			{
				currentCount =>
					if (currentCount == 1)
					{
						if (closed.isSet)
							connection.close()
						else
							closeUnusedConnections()
					}
					
					currentCount - 1
			}
		}
		
		def tryClose() =
		{
			clientCount.lock
			{
				count =>
					if (count <= 0)
						connection.close()
			}
			closed.set()
		}
	}
}
