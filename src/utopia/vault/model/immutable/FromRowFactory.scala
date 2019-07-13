package utopia.vault.model.immutable

import utopia.flow.util.CollectionExtensions._
import utopia.flow.datastructure.immutable.Value
import utopia.vault.database.Connection
import utopia.vault.sql.{Condition, Limit, OrderBy, SelectAll, SqlSegment, Where}

/**
  * These factories are used for converting database row data into objects. These factories are able to parse an object
  * from a single row (which may contain joined data)
  * @author Mikko Hilpinen
  * @since 10.7.2019, v1.2+
  */
trait FromRowFactory[+A] extends FromResultFactory[A]
{
	// ABSTRACT	--------------------
	
	/**
	  * Converts a row into an object
	  * @param row Row to be converted
	  * @return Parsed object. None if no object could be parsed.
	  */
	def apply(row: Row): Option[A]
	
	
	// IMPLEMENTED	----------------
	
	override def apply(result: Result): Vector[A] = result.rows.flatMap { r => apply(r) }
	
	
	// OTHER	--------------------
	
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
		connection(SelectAll(target) + Where(where) + Limit(1)).rows.headOption.flatMap(apply)
	}
	
	/**
	  * Finds the top / max row / model based on provided ordering column
	  * @param orderColumn A column based on which ordering is made
	  * @param connection Database connection
	  */
	def getMax(orderColumn: Column)(implicit  connection: Connection) = getWithOrder(OrderBy.descending(orderColumn))
	
	/**
	  * Finds top / max row / model based on provided ordering property. Uses primary table's columns by default but may
	  * use columns from other tables if such property couldn't be found from the primary table.
	  * @param orderProperty The name of the ordering property
	  * @param connection Database connection
	  */
	def getMax(orderProperty: String)(implicit connection: Connection): Option[A] = findColumn(orderProperty).flatMap(getMax)
	
	/**
	  * Finds the bottom / min row / model based on provided ordering column
	  * @param orderColumn A column based on which ordering is made
	  * @param connection Database connection
	  */
	def getMin(orderColumn: Column)(implicit connection: Connection) = getWithOrder(OrderBy.ascending(orderColumn))
	
	/**
	  * Finds bottom / min row / model based on provided ordering property. Uses primary table's columns by default but may
	  * use columns from other tables if such property couldn't be found from the primary table.
	  * @param orderProperty The name of the ordering property
	  * @param connection Database connection
	  */
	def getMin(orderProperty: String)(implicit connection: Connection): Option[A] = findColumn(orderProperty).flatMap(getMin)
	
	private def getWithOrder(orderBy: SqlSegment)(implicit connection: Connection) =
	{
		connection(SelectAll(target) + orderBy + Limit(1)).rows.headOption.flatMap(apply)
	}
	
	private def findColumn(propertyName: String) = table.find(propertyName).orElse {
		joinedTables.findMap { _.find(propertyName) } }
}
