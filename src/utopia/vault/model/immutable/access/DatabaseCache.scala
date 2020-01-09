package utopia.vault.model.immutable.access

import utopia.flow.caching.multi.{Cache, CacheLike, ExpiringCache, TryCache}
import utopia.flow.util.TimeExtensions._
import utopia.flow.util.CollectionExtensions._
import utopia.vault.database.ConnectionPool
import utopia.vault.sql.Condition

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration
import scala.util.Try

/**
 * Used for accessing database data. Caches retrieved data to optimize further requests
 * @author Mikko Hilpinen
 * @since 9.1.2020, v1.4
 * @param connectionPool Connection pool used when requesting new values from the database
 * @param accessor Database accessor used for performing actual data requests
 * @param maxCacheDuration Maximum duration for cached items. Infinite duration means that cached data never
 *                         expires (default)
 * @param maxFailureCacheDuration Maximum duration for failed requests. Infinite duration means that cached failures
 *                                never expire (unless maxCacheDuration is specified) and will fail in the future
 *                                as well (default).
 * @param keyToCondition A function for transforming provided keys to database search conditions
 * @param exc Execution context the connection pool uses (implicit)
 */
class DatabaseCache[I, A, Key](connectionPool: ConnectionPool, accessor: SingleAccess[I, A],
							   maxCacheDuration: Duration = Duration.Inf, maxFailureCacheDuration: Duration = Duration.Inf)
							  (keyToCondition: Key => Condition)(implicit exc: ExecutionContext) extends CacheLike[Key, Try[A]]
{
	// ATTRIBUTES	-----------------------
	
	private val cache =
	{
		// Creates a different type of cache based on specified parameters
		maxCacheDuration.finite match
		{
			case Some(maxTime) =>
				maxFailureCacheDuration.finite match
				{
					case Some(maxFailTime) => TryCache.expiring(maxFailTime, maxTime)(request)
					case None => ExpiringCache(maxTime)(request)
				}
			case None =>
				maxFailureCacheDuration.finite match
				{
					case Some(maxFailTime) => TryCache(maxFailTime)(request)
					case None => Cache(request)
				}
		}
	}
	
	
	// IMPLEMENTED	----------------------
	
	override def apply(key: Key) = cache(key)
	
	override def cached(key: Key) = cache.cached(key)
	
	
	// OTHER	--------------------------
	
	private def request(key: Key) =
	{
		connectionPool.tryWith { implicit connection =>
			val condition = keyToCondition(key)
			accessor.find(condition).toTry(
				new NoSuchElementException(s"No value for key '$key'. Using search condition: $condition"))
		}.flatten
	}
}