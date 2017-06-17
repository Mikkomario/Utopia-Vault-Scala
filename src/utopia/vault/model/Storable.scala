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
    
    
    // OTHER METHODS    ------------------------------
    
    def toModel(includeEmpty: Boolean = false) = 
    {
        val properties = table.columns.flatMap(column => 
        {
            val value = valueForProperty(column.name)
            if (value.isEmpty && !includeEmpty) None else Some((column.name, value))
        } )
        
        Model(properties, declaration)
    }
    
    def push(writeNulls: Boolean = false)(implicit connection: Connection) = 
    {
        // Either inserts as a new row or updates an existing row
        val index = this.index
        
        if (index.isDefined)
        {
            val indexColumn = table.primaryColumn.get
            connection(Update(table, toModel(writeNulls) - indexColumn.name) + Where(indexColumn <=> index))
        }
        else if (table.usesAutoIncrement) 
        {
            // TODO: What is done with the newly generated index?
        }
        // TODO: No insert or update possible! Return something to indicate?
    }
}