package utopia.vault.model.immutable.factory

import utopia.flow.datastructure.immutable.Value
import utopia.vault.database.Connection
import utopia.vault.model.immutable.{Result, Row, Table}
import utopia.vault.sql.{Condition, Exists, JoinType, SelectAll, SqlTarget, Where}

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
	  * Parses a result into one or multiple (or zero) objects
	  * @param result A database query result to be parsed
	  * @return Parsed objects
	  */
	def apply(result: Result): Vector[A]
	
	
	// COMPUTED	---------------
	
	/**
	  * @return This factory's target that includes the primary table and possible joined tables
	  */
	def target = joinedTables.foldLeft(table: SqlTarget) { (r, t) => r.join(t, JoinType.Left) }
	
	/**
	  * @return The table(s) used by this factory (never empty)
	  */
	def tables = table +: joinedTables
	
	
	// OTHER	---------------
	
	/**
	  * Finds possibly multiple instances from the database
	  * @param where the condition with which the instances are filtered
	  * @return Parsed instance data
	  */
	def getMany(where: Condition)(implicit connection: Connection) =
	{
		apply(connection(SelectAll(target) + Where(where)))
	}
	
	/**
	  * Finds every single instance of this type from the database. This method should only be
	  * used in case of somewhat small tables.
	  * @see #getMany(Condition)
	  */
	def getAll()(implicit connection: Connection) = apply(connection(SelectAll(target)))
	
	/**
	  * Checks whether an object exists for the specified query
	  * @param where A search condition
	  * @param connection Database connection (implicit)
	  * @return Whether there exists data in the DB for specified condition
	  */
	def exists(where: Condition)(implicit connection: Connection) = Exists(target, where)
	
	/**
	  * Checks whether there exists data for the specified index
	  * @param index An index in this factory's primary table
	  * @param connection Database connection (implicit)
	  * @return Whether there exists data for the specified index
	  */
	def exists(index: Value)(implicit connection: Connection) = Exists.index(table, index)
	
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
		apply(connection(SelectAll(target) + Where(where))).headOption
	}
}
