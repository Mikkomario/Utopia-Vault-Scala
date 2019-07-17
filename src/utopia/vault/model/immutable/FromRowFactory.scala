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
	  * @param where Additional search condition (optional)
	  * @param connection Database connection
	  */
	def getMax(orderColumn: Column, where: Option[Condition])(implicit  connection: Connection) =
		getWithOrder(OrderBy.descending(orderColumn), where)
	
	/**
	  * Finds the top / max row / model based on provided ordering column
	  * @param orderColumn A column based on which ordering is made
	  * @param connection Database connection
	  */
	def getMax(orderColumn: Column)(implicit  connection: Connection): Option[A] = getMax(orderColumn, None)
	
	/**
	  * Finds the top / max row / model based on provided ordering column
	  * @param orderColumn A column based on which ordering is made
	  * @param where Additional search condition (optional)
	  * @param connection Database connection
	  */
	def getMax(orderColumn: Column, where: Condition)(implicit  connection: Connection): Option[A] = getMax(orderColumn, Some(where))
	
	/**
	  * Finds top / max row / model based on provided ordering property. Uses primary table's columns by default but may
	  * use columns from other tables if such property couldn't be found from the primary table.
	  * @param orderProperty The name of the ordering property
	  * @param connection Database connection
	  */
	def getMax(orderProperty: String)(implicit connection: Connection): Option[A] = findColumn(orderProperty).flatMap(getMax)
	
	/**
	  * Finds top / max row / model based on provided ordering property. Uses primary table's columns by default but may
	  * use columns from other tables if such property couldn't be found from the primary table.
	  * @param orderProperty The name of the ordering property
	  * @param where Additional search condition (optional)
	  * @param connection Database connection
	  */
	def getMax(orderProperty: String, where: Condition)(implicit connection: Connection): Option[A] =
		findColumn(orderProperty).flatMap { getMax(_, where) }
	
	/**
	  * Finds the bottom / min row / model based on provided ordering column
	  * @param orderColumn A column based on which ordering is made
	  * @param connection Database connection
	  */
	def getMin(orderColumn: Column, where: Option[Condition])(implicit connection: Connection) =
		getWithOrder(OrderBy.ascending(orderColumn), where)
	
	/**
	  * Finds the bottom / min row / model based on provided ordering column
	  * @param orderColumn A column based on which ordering is made
	  * @param connection Database connection
	  */
	def getMin(orderColumn: Column)(implicit connection: Connection): Option[A] = getMin(orderColumn, None)
	
	/**
	  * Finds the bottom / min row / model based on provided ordering column
	  * @param orderColumn A column based on which ordering is made
	  * @param connection Database connection
	  */
	def getMin(orderColumn: Column, where: Condition)(implicit connection: Connection): Option[A] = getMin(orderColumn, Some(where))
	
	/**
	  * Finds bottom / min row / model based on provided ordering property. Uses primary table's columns by default but may
	  * use columns from other tables if such property couldn't be found from the primary table.
	  * @param orderProperty The name of the ordering property
	  * @param connection Database connection
	  */
	def getMin(orderProperty: String)(implicit connection: Connection): Option[A] = findColumn(orderProperty).flatMap(getMin)
	
	/**
	  * Finds bottom / min row / model based on provided ordering property. Uses primary table's columns by default but may
	  * use columns from other tables if such property couldn't be found from the primary table.
	  * @param orderProperty The name of the ordering property
	  * @param connection Database connection
	  */
	def getMin(orderProperty: String, where: Condition)(implicit connection: Connection): Option[A] =
		findColumn(orderProperty).flatMap { getMin(_, where) }
	
	private def getWithOrder(orderBy: SqlSegment, where: Option[Condition] = None)(implicit connection: Connection) =
	{
		val beginning = SelectAll(target)
		val end = orderBy + Limit(1)
		val statement = where.map { beginning + Where(_) + end }.getOrElse { beginning + end }
		
		connection(statement).rows.headOption.flatMap(apply)
	}
	
	private def findColumn(propertyName: String) = table.find(propertyName).orElse {
		joinedTables.findMap { _.find(propertyName) } }
}
