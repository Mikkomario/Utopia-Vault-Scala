package utopia.vault.nosql.access

import utopia.vault.database.Connection
import utopia.vault.model.immutable.Column
import utopia.vault.sql.OrderDirection.{Ascending, Descending}
import utopia.vault.sql.{Condition, OrderBy, OrderDirection}

/**
 * A common trait for access points that return individual instances
 * @author Mikko Hilpinen
 * @since 30.1.2020, v1.4
 */
trait SingleAccess[+A, +Repr] extends FilterableAccess[Option[A], Repr]
{
	// OTHER	---------------------
	
	/**
	 * The "top" value based on specified ordering
	 * @param orderColumn Ordering column
	 * @param orderDirection Ordering direction
	 * @param additionalCondition a search condition
	 * @param connection Database connection (implicit)
	 * @return The "top" (first result) item based on provided ordering and search condition.
	 */
	def top(orderColumn: Column, orderDirection: OrderDirection, additionalCondition: Option[Condition])
		   (implicit connection: Connection) = read(mergeCondition(additionalCondition),
		Some(OrderBy(orderColumn, orderDirection)))
	
	/**
	 * The "top" value based on specified ordering
	 * @param orderColumn Ordering column
	 * @param orderDirection Ordering direction
	 * @param connection Database connection (implicit)
	 * @return The "top" (first result) item based on provided ordering.
	 */
	def top(orderColumn: Column, orderDirection: OrderDirection)(implicit connection: Connection): Option[A] =
		top(orderColumn, orderDirection, None)
	
	/**
	 * The minimum value based on specified ordering
	 * @param column Ordering column
	 * @param additionalCondition a search condition
	 * @param connection Database connection (implicit)
	 * @return The smallest item based on provided ordering and search condition.
	 */
	def minBy(column: Column, additionalCondition: Option[Condition])(implicit connection: Connection) =
		top(column, Ascending, additionalCondition)
	
	/**
	 * The minimum value based on specified ordering
	 * @param column Ordering column
	 * @param connection Database connection (implicit)
	 * @return The smallest item based on provided ordering.
	 */
	def minBy(column: Column)(implicit connection: Connection): Option[A] = minBy(column, None)
	
	/**
	 * The minimum value based on specified ordering
	 * @param propertyName Name of ordering property
	 * @param additionalCondition a search condition
	 * @param connection Database connection (implicit)
	 * @return The smallest item based on provided ordering and search condition.
	 */
	def minBy(propertyName: String, additionalCondition: Option[Condition])(implicit connection: Connection): Option[A] =
		minBy(table(propertyName), additionalCondition)
	
	/**
	 * The minimum value based on specified ordering
	 * @param propertyName Name of ordering property
	 * @param connection Database connection (implicit)
	 * @return The smallest item based on provided ordering.
	 */
	def minBy(propertyName: String)(implicit connection: Connection): Option[A] = minBy(propertyName, None)
	
	/**
	 * The maximum value based on specified ordering
	 * @param column Ordering column
	 * @param additionalCondition a search condition
	 * @param connection Database connection (implicit)
	 * @return The largest item based on provided ordering and search condition.
	 */
	def maxBy(column: Column, additionalCondition: Option[Condition])(implicit connection: Connection) =
		top(column, Descending, additionalCondition)
	
	/**
	 * The maximum value based on specified ordering
	 * @param column Ordering column
	 * @param connection Database connection (implicit)
	 * @return The largest item based on provided ordering.
	 */
	def maxBy(column: Column)(implicit connection: Connection): Option[A] = maxBy(column, None)
	
	/**
	 * The maximum value based on specified ordering
	 * @param propertyName Name of ordering property
	 * @param additionalCondition a search condition
	 * @param connection Database connection (implicit)
	 * @return The largest item based on provided ordering and search condition.
	 */
	def maxBy(propertyName: String, additionalCondition: Option[Condition])(implicit connection: Connection): Option[A] =
		maxBy(table(propertyName), additionalCondition)
	
	/**
	 * The maximum value based on specified ordering
	 * @param propertyName Name of ordering property
	 * @param connection Database connection (implicit)
	 * @return The largest item based on provided ordering.
	 */
	def maxBy(propertyName: String)(implicit connection: Connection): Option[A] = maxBy(propertyName, None)
	
	/**
	 * Finds an individual item with a search condition
	 * @param condition A search condition
	 * @param ordering Applied ordering (optional, None by default)
	 * @param connection DB Connection
	 * @return The item found using specified condition. None if no item was found.
	 */
	def find(condition: Condition, ordering: Option[OrderBy] = None)(implicit connection: Connection): Option[A] =
		read(Some(mergeCondition(condition)), ordering)
}
