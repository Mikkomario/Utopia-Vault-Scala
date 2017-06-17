package utopia.vault.model

import utopia.flow.datastructure.immutable.Value
import utopia.vault.database.Connection
import utopia.flow.datastructure.immutable.Constant
import utopia.flow.datastructure.immutable.Model
import utopia.flow.generic.DeclarationConstantGenerator
import utopia.vault.sql.Update
import utopia.vault.sql.Where
import utopia.vault.sql.Insert
import utopia.vault.sql.Condition

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
        })
        
        Model(properties, new DeclarationConstantGenerator(declaration))
    }
    
    /**
     * Converts this storable instance's properties into a condition. The condition checks that 
     * each of the instances DEFINED properties match their value in the database. Does not include 
     * any null / empty properties.
     * @return a condition based on this storable instance. None if the instance didn't contain 
     * any properties that could be used for forming a condition
     */
    def toCondition() = 
    {
        val conditions = table.columns.flatMap(column => 
        {
            val value = valueForProperty(column.name)
            if (value.isEmpty) None else Some(column <=> value)
        })
        
        conditions.headOption.map { _ && conditions.tail }
    }
    
    /**
     * Pushes the storable instance's data to the database using either insert or update. In case 
     * the instance doesn't have a specified index AND it's table uses indexing that is not 
     * auto-increment, cannot push data and returns None.
     * @param writeNulls Whether empty / null values should be pushed to the database (on update). 
     * False by default, which means that columns will never be specifically set to null. Use 
     * true if you specifically want to set a column to null.
     * @return The (new) index of the instance. In case of auto-increment table, this index was 
     * just generated. None if no push was made due to lack of index / identification.
     */
    def push(writeNulls: Boolean = false)(implicit connection: Connection) = 
    {
        // Either inserts as a new row or updates an existing row
        if (update(writeNulls))
        {
            Some(index)
        }
        else if (table.usesAutoIncrement) 
        {
            connection(Insert(table, toModel())).generatedKeys.headOption
        }
        else if (table.primaryColumn.isEmpty)
        {
            // If the table doesn't have an index, just inserts every time
            connection(Insert(table, toModel()))
            None
        }
        else 
        {
            None
        }
    }
    
    /**
     * Pushes storable data to the database, but only allows updating of an existing row. No 
     * inserts will be made. This will only succeed if the instance has a defined index.
     * @param writeNulls whether null values should be updated to the database. Defaults to false.
     * @return whether the database was actually updated
     */
    def update(writeNulls: Boolean = false)(implicit connection: Connection) = 
    {
        val index = this.index
        if (index.isDefined)
        {
            connection(toUpdateStatement(writeNulls) + Where(table.primaryColumn.get <=> index))
            true
        }
        else 
        {
            false
        }
    }
    
    /**
     * Creates an update sql segment based on this storable instance. This update segment can then 
     * be combined with a condition in order to update row data to match that of this storable instance.
     * @param writeNulls whether null / empty value assignments should be included in the update segment. 
     * Defaults to false.
     * @param writeIndex whether index should specifically be included among the set column values 
     * (where applicable). Defaults to false.
     */
    def toUpdateStatement(writeNulls: Boolean = false, writeIndex: Boolean = false) = 
    {
        val primaryColumn = table.primaryColumn
        val updateModel = if (writeIndex || primaryColumn.isEmpty) 
                toModel(writeNulls) else toModel(writeNulls) - primaryColumn.get.name
        Update(table, updateModel)
    }
    
    /**
     * Pushes storable data to the database, but will always insert the instance as a new row 
     * instead of updating an existing row. This will only work for tables that use auto-increment 
     * indexing or no indexing at all.
     * @return A option with the depth of two. The first layer determines whether an insert was 
     * actually performed, and the second layer provides access to the generated key, where applicable.
     * For example, returns None if no operation was performed, returns Some(None) if an operation 
     * was performed but no index generated (for tables with no indices) and Some(Some(...)) when 
     * an operation was performed and a key generated.
     */
    def insert(implicit connection: Connection) = 
    {
        // Only works with tables that use auto-increment indexing or no indices at all
        if (table.primaryColumn.isDefined)
        {
            if (table.usesAutoIncrement)
            {
                Some(connection(Insert(table, toModel() - table.primaryColumn.get.name)).generatedKeys.headOption)
            }
            else 
            {
                None
            }
        }
        else
        {
            connection(Insert(table, toModel()))
            Some(None)
        }
    }
}