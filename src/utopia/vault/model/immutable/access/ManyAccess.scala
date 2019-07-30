package utopia.vault.model.immutable.access

import utopia.vault.sql.Extensions._
import utopia.vault.database.Connection
import utopia.vault.model.immutable.factory.FromResultFactory
import utopia.vault.sql.{Condition, ConditionElement}

/**
 * Used for accessing multiple instances or ids of one or more tables
 * @author Mikko Hilpinen
 * @since 30.7.2019, v1.3+
 * @tparam I Type of used index
 * @tparam A Type of read model
 */
trait ManyAccess[-I, +A] extends Access[I, A, FromResultFactory[A]]
{
	// OPERATORS	-------------------
	
	/**
	 * Finds multiple items based on a condition
	 * @param condition A condition
	 * @param connection Database connection (implicit)
	 * @return Read item(s)
	 */
	protected def apply(condition: Condition)(implicit connection: Connection) = factory.getMany(condition)
	
	
	// OTHER	-----------------------
	
	/**
	 * Finds models for multiple ids
	 * @param ids Searched ids
	 * @param connection Database connection (implicit)
	 * @tparam I2 Type of searched id
	 * @return Models for searched ids
	 */
	def withIds[I2 <: I](ids: Set[I2])(implicit connection: Connection) =
	{
		if (ids.isEmpty)
			Vector()
		else
			factory.getMany(index in ids.map { idValue(_): ConditionElement })
	}
}
