package utopia.vault.model.immutable.access

import utopia.vault.database.Connection
import utopia.vault.model.immutable.factory.FromResultFactory
import utopia.vault.sql.Condition

/**
 * Provides access to possibly multiple items based on a search condition
 * @author Mikko Hilpinen
 * @since 6.10.2019, v1.3.1+
 */
class ConditionalManyAccess[+A](override val condition: Condition, override val factory: FromResultFactory[A]) extends ConditionalAccess[A]
{
	/**
	 * Reads items from database
	 * @param connection DB Connection
	 * @return all items accessed from this sub group
	 */
	def get(implicit connection: Connection) = factory.getMany(condition)
	
	/**
	 * Finds certain items based on an additional search condition
	 * @param additionalCondition An additional search condition
	 * @param connection DB Connection
	 * @return Items that fulfill both current and additional search conditions
	 */
	def find(additionalCondition: Condition)(implicit connection: Connection) = factory.getMany(
		condition && additionalCondition)
	
	/**
	 * Provides access to items under an additional search condition
	 * @param additionalCondition An additional search condition
	 * @return Access to items that fulfill both current and additional search conditions
	 */
	def subGroup(additionalCondition: Condition) = new ConditionalManyAccess[A](condition && additionalCondition, factory)
}
