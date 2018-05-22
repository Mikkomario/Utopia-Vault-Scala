package utopia.vault.model

import utopia.flow.datastructure.template

import utopia.flow.datastructure.mutable.Model
import utopia.flow.datastructure.mutable.Variable
import utopia.flow.generic.DeclarationVariableGenerator
import utopia.flow.datastructure.immutable.Value
import utopia.flow.datastructure.template.Property

object StorableModel
{
    /**
     * Creates a new factory for storable models of a certain table
     */
    def makeFactory(table: Table) = new StorableModelFactory(table)
}

/**
* These mutable models can be used as simple storable instances
* @author Mikko Hilpinen
* @since 22.5.2018
**/
class StorableModel(override val table: Table) extends Model[Variable](
        new DeclarationVariableGenerator(table.toModelDeclaration)) with Storable
{
    // COMPUTED    -------------------
    
	override def valueProperties = attributes.map(v => v.name -> v.value)
	
	/**
	 * Updates the index for this model
	 */
	def index_=(newIndex: Value) = table.primaryColumn.foreach(
	        c => this += new Variable(c.name, newIndex))
}

/**
 * These factories are used for constructing storable models from table data
 */
class StorableModelFactory(val table: Table) extends StorableFactory[StorableModel]
{
    def apply(model: template.Model[Property]) = 
    {
        val storable = new StorableModel(table)
        storable ++= model.attributes.map(p => new Variable(p.name, p.value))
        
        Some(storable)
    }
}