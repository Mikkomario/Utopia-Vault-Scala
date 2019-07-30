package utopia.vault.model.immutable

import utopia.flow.datastructure.immutable
import utopia.flow.datastructure.immutable.Constant
import utopia.flow.util.CollectionExtensions._
import utopia.flow.generic.FromModelFactoryWithSchema
import utopia.vault.util.ErrorHandling

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
trait StorableFactory[+A] extends FromRowFactory[A] with FromModelFactoryWithSchema[A]
{
    // IMPLEMENTED  ----------------------------
    
    override val joinedTables = Vector()
    
    override def schema = table.requirementDeclaration
    
    // Handles parsing errors
    override def apply(row: Row) =
    {
        val result = apply(row(table))
        result.failure.foreach { e => ErrorHandling.modelParsePrinciple.handle(e) }
        result.toOption
    }
}

private class ImmutableStorableFactory(override val table: Table) extends StorableFactory[Storable]
{
    override protected def fromValidatedModel(model: immutable.Model[Constant]) = Storable(table, model)
}