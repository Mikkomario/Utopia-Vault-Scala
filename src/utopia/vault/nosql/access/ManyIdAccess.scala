package utopia.vault.nosql.access
import utopia.flow.datastructure.immutable.Value
import utopia.vault.database.Connection
import utopia.vault.sql.{Condition, OrderBy, Select, Where}

/**
 * Used for accessing multiple ids at a time
 * @author Mikko Hilpinen
 * @since 30.1.2020, v1.4
 */
trait ManyIdAccess[+ID] extends IdAccess[ID, Vector[ID]] with ManyAccess[ID, ManyIdAccess[ID]]
{
	// IMPLEMENTED	-----------------------
	
	override protected def read(condition: Option[Condition], order: Option[OrderBy])(implicit connection: Connection) =
	{
		val statement = Select.index(target, table) + condition.map { Where(_) } + order
		connection(statement).rowValues.flatMap(valueToId).distinct
	}
	
	override def filter(additionalCondition: Condition): ManyIdAccess[ID] = new Filtered(additionalCondition)
	
	
	// NESTED	---------------------------
	
	private class Filtered(condition: Condition) extends ManyIdAccess[ID]
	{
		override def target = ManyIdAccess.this.target
		
		override def valueToId(value: Value) = ManyIdAccess.this.valueToId(value)
		
		override def table = ManyIdAccess.this.table
		
		override def globalCondition = Some(ManyIdAccess.this.mergeCondition(condition))
	}
}
