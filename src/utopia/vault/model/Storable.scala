package utopia.vault.model

import utopia.flow.datastructure.immutable.Value
import utopia.vault.database.Connection
import utopia.flow.datastructure.immutable.Constant
import utopia.flow.datastructure.immutable.Model
import utopia.flow.generic.DeclarationConstantGenerator
import utopia.vault.sql.Update
import utopia.vault.sql.Where
import utopia.vault.sql.Insert

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
    
    /**
     * The index of this storable instance. Index is the primary method way to identify the 
     * instance in database context.
     */
    def index = table.primaryColumn.map { column => valueForProperty(column.name) }.getOrElse(Value.empty())
    
    /**
     * A declaration that describes this instance. The declaration is based on the instance's table
     */
    def declaration = table.toModelDeclaration
    
    
    // OTHER METHODS    ------------------------------
    
    /**
     * Generates a model that represents this storable instance
     * @param includeEmpty should empty property values be included in the final model. 
     * Default is false.
     */
    def toModel(includeEmpty: Boolean = false) = 
    {
        val properties = table.columns.flatMap(column => 
        {
            val value = valueForProperty(column.name)
            if (value.isEmpty && !includeEmpty) None else Some((column.name, value))
        } )
        
        Model(properties, new DeclarationConstantGenerator(declaration))
    }
    
    /**
     * Pushes the storable instance's data to the database using either insert or update. In case 
     * the instance doesn't have a specified index AND it's table doesn't use auto-increment 
     * indexing, cannot push data and returns None.
     * @param writeNulls Whether empty / null values should be pushed to the database (on update). 
     * False by default, which means that columns will never be specifically set to null. Use 
     * true if you specifically want to set a column to null.
     * @return The (new) index of the instance. In case of auto-increment table, this index was 
     * just generated. None if no push was made due to lack of index / identification.
     */
    def push(writeNulls: Boolean = false)(implicit connection: Connection) = 
    {
        // Either inserts as a new row or updates an existing row
        val index = this.index
        
        if (index.isDefined)
        {
            val indexColumn = table.primaryColumn.get
            connection(Update(table, toModel(writeNulls) - indexColumn.name) + Where(indexColumn <=> index))
            Some(index)
        }
        else if (table.usesAutoIncrement) 
        {
            connection(Insert(table, toModel())).generatedKeys.headOption
        }
        else
        {
            // TODO: In case the table doesn't use indexing, just insert the model
            None
        }
    }
    
    // TODO: Update(where) and insert
}