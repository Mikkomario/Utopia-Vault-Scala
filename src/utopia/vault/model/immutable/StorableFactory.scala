package utopia.vault.model.immutable

import utopia.flow.datastructure.template.{Model, Property}
import utopia.flow.generic.FromModelFactory

object StorableFactory
{
    /**
      * Creates a new simple storable factory
      * @param table The target table
      * @return A factory for that table
      */
    def apply(table: Table): StorableFactory[Storable] = new ImmutableStorableFactory(table)
}

/**
 * These factory instances are used for converting database-originated model data into a 
 * storable instance.
 * @author Mikko Hilpinen
 * @since 18.6.2017
 */
trait StorableFactory[+T] extends FromResultFactory[T] with FromModelFactory[T]
{
    // IMPLEMENTED  ----------------------------
    
    override val joinedTables = Vector()
    
    override def parseSingle(result: Result) = result.rows.headOption.map { _.toModel }.flatMap(apply)
    
    override def parseMany(result: Result) = result.rowModels.flatMap(apply)
}

private class ImmutableStorableFactory(override val table: Table) extends StorableFactory[Storable]
{
    override def apply(model: Model[Property]) = Some(Storable(table, model))
}