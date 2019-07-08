package utopia.vault.model.immutable

import utopia.flow.datastructure.immutable.Value
import utopia.vault.database.Connection
import utopia.vault.sql.{Condition, JoinType, Limit, SelectAll, SqlTarget, Where}

/**
  * These factories are used for constructing object data from database results
  * @author Mikko Hilpinen
  * @since 8.7.2019, v1.1.1+
  */
trait FromResultFactory[+A]
{
	// ABSTRACT	---------------
	
	/**
	  * @return The primary table used for reading results data for this factory
	  */
	def table: Table
	
	/**
	  * @return The tables that are joined for complete results
	  */
	def joinedTables: Seq[Table]
	
	/**
	  * Parses a result into a single object
	  * @param result The result to be parsed
	  * @return Parsed object. None if the result couldn't be parsed into an object.
	  */
	def parseSingle(result: Result): Option[A]
	
	/**
	  * Parses a result into multiple objects
	  * @param result The result to be parsed
	  * @return Parsed objects
	  */
	def parseMany(result: Result): Vector[A]
	
	
	// COMPUTED	---------------
	
	private def target = joinedTables.foldLeft(table: SqlTarget) { (r, t) => r.join(t, JoinType.Left) }
	
	
	// OTHER	---------------
	
	/**
	  * Retrieves an object's data from the database and parses it to a proper instance
	  * @param index the index / primary key with which the data is read
	  * @return database data parsed into an instance. None if there was no data available.
	  */
	def get(index: Value)(implicit connection: Connection): Option[A] =
	{
		table.primaryColumn.flatMap { column => get(column <=> index) }
	}
	
	/**
	  * Retrieves an object's data from the database and parses it to a proper instance
	  * @param where The condition with which the row is found from the database (will be limited to
	  * the first result row)
	  * @return database data parsed into an instance. None if no data was found with the provided
	  * condition
	  */
	def get(where: Condition)(implicit connection: Connection) =
	{
		parseSingle(connection(SelectAll(target) + Where(where) + Limit(1)))
	}
	
	/**
	  * Finds possibly multiple instances from the database
	  * @param where the condition with which the instances are filtered
	  * @return Parsed instance data
	  */
	def getMany(where: Condition)(implicit connection: Connection) =
	{
		parseMany(connection(SelectAll(target) + Where(where)))
	}
	
	/**
	  * Finds every single instance of this type from the database. This method should only be
	  * used in case of somewhat small tables.
	  * @see #getMany(Condition)
	  */
	def getAll()(implicit connection: Connection) = parseMany(connection(SelectAll(table)))
}
