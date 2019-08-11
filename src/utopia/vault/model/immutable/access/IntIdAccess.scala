package utopia.vault.model.immutable.access

import utopia.flow.datastructure.immutable.Value

/**
 * This implementation of SingleIdAccess supports int ids
 * @author Mikko Hilpinen
 * @since 30.7.2019, v1.3+
 */
trait IntIdAccess extends IdAccess[Int]
{
	override protected def valueToId(value: Value) = value.getInt
}
