package utopia.vault.model

import utopia.flow.datastructure.immutable.Value
import utopia.vault.database.Connection
import utopia.flow.datastructure.immutable.Constant
import utopia.flow.datastructure.immutable.Model
import utopia.flow.generic.DeclarationConstantGenerator
import utopia.vault.sql.Update
import utopia.vault.sql.Where

/**
 * Storable instances can be stored into a database table.
 * @author Mikko Hilpinen
 * @since 10.6.2017
 */
trait Storable
{
    // ABSTRACT PROPERTIES & METHODS    --------------
    
    /**
     * The table the instance uses to store its data
     */
    def table: Table
    
    /**
     * This methods is used for retrieving property data from the model
     */
    def valueForProperty(propertyName: String): Value
    
    
    // COMPUTED PROPERTIES    ------------------------
    
    def index = table.primaryColumn.map { column => valueForProperty(column.name) }.getOrElse(Value.empty())
    
    def declaration = new DeclarationConstantGenerator(table.toModelDeclaration)
    
    def toModel = 
    {
        val properties = table.columns.flatMap(column => 
        {
            val value = valueForProperty(column.name)
            if (value.isEmpty) None else Some((column.name, value))
        } )
        
        Model(properties, declaration)
    }
    
    
    // OTHER METHODS    ------------------------------
    
    def push(implicit connection: Connection) = 
    {
        // Either inserts as a new row or updates an existing row
        val index = this.index
        
        if (index.isDefined)
        {
            // TODO: This implementation doesn't overwrite with null (should it?)
            val indexColumn = table.primaryColumn.get
            connection(Update(table, toModel - indexColumn.name) + Where(indexColumn <=> index))
        }
        else if (table.usesAutoIncrement) 
        {
            // TODO: What is done with the newly generated index?
        }
        // TODO: No insert or update possible! Return something to indicate?
    }
}