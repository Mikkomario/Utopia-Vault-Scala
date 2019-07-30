package utopia.vault.model.immutable.access

import utopia.flow.datastructure.immutable.Value
import utopia.vault.model.immutable.factory.FromResultFactory

/**
 * Used for interacting with items in one or more tables
 * @author Mikko Hilpinen
 * @since 30.7.2019, v1.3+
 * @tparam I Type of used index
 * @tparam A Type of read model
 */
trait Access[-I, +A, +Factory <: FromResultFactory[A]]
{
	// ABSTRACT	-----------------------
	
	/**
	 * Converts an id-value into a value
	 * @param id Source id-value
	 * @return Id-value wrapped in value
	 */
	protected def idValue(id: I): Value
	
	/**
	 * @return Factory used by this access
	 */
	def factory: Factory
	
	
	// COMPUTED	-----------------------
	
	/**
	 * @return The (primary) table used by this access
	 */
	def table = factory.table
	
	/**
	 * @return The primary column of the (primary) table used by this access
	 */
	def index = table.primaryColumn.get
}
