package utopia.vault.model.immutable.access

import utopia.vault.database.Connection
import utopia.vault.model.immutable.factory.FromRowFactory
import utopia.vault.sql.Condition

/**
 * Used for interacting with singular items in one or more tables
 * @author Mikko Hilpinen
 * @since 30.7.2019, v1.3+
 * @tparam I Type of used index
 * @tparam A Type of read model
 */
trait SingleAccess[-I, +A] extends Access[I, A, FromRowFactory[A]]
{
	// OPERATORS	--------------------
	
	/**
	 * Finds a single item based on a condition
	 * @param condition A search condition
	 * @param connection Database connection (implicit)
	 * @return Search results
	 */
	def apply(condition: Condition)(implicit connection: Connection) = factory.get(condition)
	
	
	// OTHER	------------------------
	
	/**
	 * Reads a model with an id
	 * @param id Searched id
	 * @param connection Database connection (implicit)
	 * @return A model with specified id
	 */
	def withId(id: I)(implicit connection: Connection) = factory.get(idValue(id))
}
