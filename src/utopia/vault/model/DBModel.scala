package utopia.vault.model

import utopia.flow.datastructure.template
import utopia.flow.datastructure.immutable

import utopia.flow.datastructure.mutable.Model
import utopia.flow.datastructure.mutable.Variable
import utopia.flow.generic.DeclarationVariableGenerator
import utopia.flow.datastructure.immutable.Value
import utopia.flow.datastructure.template.Property
import utopia.flow.datastructure.immutable.Constant

object DBModel
{
    /**
     * Creates a new factory for storable models of a certain table
     */
    def makeFactory(table: Table) = new DBModelFactory(table)
    
    /**
     * Wraps a model into a db model
     */
    def apply(table: Table, model: template.Model[Property]) = 
    {
        val result = new DBModel(table)
        result.set(model)
        result
    }
}

/**
* These mutable models can be used as simple storable instances
* @author Mikko Hilpinen
* @since 22.5.2018
**/
class DBModel(override val table: Table) extends Model[Variable](
        new DeclarationVariableGenerator(table.toModelDeclaration)) with Storable with Readable
{
    // COMPUTED    -------------------
    
	override def valueProperties = attributes.map(v => v.name -> v.value)
	
	override def set(data: template.Model[Property]) = update(data)
}

/**
 * These factories are used for constructing storable models from table data
 */
class DBModelFactory(val table: Table) extends StorableFactory[DBModel]
{
    def apply(model: template.Model[Property]) = 
    {
        val storable = new DBModel(table)
        storable ++= model.attributes.map(p => new Variable(p.name, p.value))
        
        Some(storable)
    }
}