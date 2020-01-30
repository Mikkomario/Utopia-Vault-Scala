package utopia.vault.nosql.access

import utopia.vault.database.Connection
import utopia.vault.sql.{Condition, OrderBy}

/**
 * A common trait for access points that return multiple items
 * @author Mikko Hilpinen
 * @since 30.1.2020, v1.4
 */
trait ManyAccess[+A, +Repr] extends FilterableAccess[Vector[A], Repr]
{
	// COMPUTED	--------------------
	
	/**
	 * @param connection Implicit database connection
	 * @return All items that can be accessed through this access point
	 */
	def all(implicit connection: Connection) = read(globalCondition)
	
	/**
	 * Finds all items that satisfy the specified search condition
	 * @param condition A search condition
	 * @param ordering Ordering applied (optional, None by default)
	 * @param connection Implicit database connection
	 * @return Read items
	 */
	def find(condition: Condition, ordering: Option[OrderBy] = None)(implicit connection: Connection) =
		read(Some(mergeCondition(condition)), ordering)
}
