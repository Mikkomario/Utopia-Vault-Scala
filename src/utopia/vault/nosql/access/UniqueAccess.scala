package utopia.vault.nosql.access

import utopia.vault.database.Connection
import utopia.vault.sql.Condition

/**
 * A common trait for access points that provide access to an individual unique item. Eg. in searches based on a
 * unique key or primary key.
 * @author Mikko Hilpinen
 * @since 30.1.2020, v1.4
 */
trait UniqueAccess[+A] extends Access[Option[A]]
{
	// ABSTRACT	---------------------
	
	/**
	 * @return The search condition used by this access point globally. Should limit the results to a single row in DB.
	 */
	def condition: Condition
	
	
	// IMPLEMENTED	-----------------
	
	override def globalCondition: Some[Condition] = Some(condition)
	
	
	// COMPUTED	---------------------
	
	/**
	 * @param connection Implicit database connection
	 * @return The unique item accessed through this access point. None if no item was found.
	 */
	def get(implicit connection: Connection) = read(globalCondition)
}
