package utopia.vault.model.immutable

import utopia.flow.datastructure.immutable.Value
import utopia.vault.database.Connection
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
}
