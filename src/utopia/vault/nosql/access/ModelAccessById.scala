package utopia.vault.nosql.access

import utopia.flow.datastructure.immutable.Value
import utopia.vault.database.Connection
import utopia.vault.model.immutable.factory.FromResultFactory
import utopia.vault.sql.{Condition, OrderBy}

/**
 * Used for accessing individual models with their ids
 * @author Mikko Hilpinen
 * @since 30.1.2020, v1.4
 */
class ModelAccessById[+A, +ID](val id: Value, val factory: FromResultFactory[A])
	extends UniqueAccess[A]
{
	override def condition = table.primaryColumn.get <=> id
	
	override def table = factory.table
	
	override protected def read(condition: Option[Condition], order: Option[OrderBy], limit: Option[Int])(
		implicit connection: Connection) = factory.get(condition.getOrElse(this.condition), order)
}
