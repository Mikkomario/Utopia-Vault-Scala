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
import utopia.flow.generic.ModelConvertible
import utopia.vault.sql.SqlSegment

/**
 * Storable instances can be stored into a database table.
 * @author Mikko Hilpinen
 * @since 10.6.2017
 */
trait Storable extends ModelConvertible
{
    // ABSTRACT PROPERTIES & METHODS    --------------
    
    /**
     * The table the instance uses to store its data
     */
    def table: Table
    
    /**
     * The model properties of this storable instance
     */
    def valueProperties: Traversable[(String, Value)]
    
    
    // COMPUTED PROPERTIES    ------------------------
    
    override def toModel = Model(valueProperties, new DeclarationConstantGenerator(declaration))
    
    /**
     * The index of this storable instance. Index is the primary method way to identify the 
     * instance in database context.
     */
    def index = table.primaryColumn.flatMap { column => valueProperties.find { case (name, _) => 
            name.equalsIgnoreCase(column.name) }.map { case (_, value) => value } }.getOrElse(Value.empty())
    
    /**
     * A declaration that describes this instance. The declaration is based on the instance's table
     */
    def declaration = table.toModelDeclaration
    
    
    // OTHER METHODS    ------------------------------
    
    /**
     * Converts this storable instance's properties into a condition. The condition checks that 
     * each of the instances DEFINED properties match their value in the database. Does not include 
     * any null / empty properties.
     * @return a condition based on this storable instance. None if the instance didn't contain 
     * any properties that could be used for forming a condition
     */
    def toCondition(limitKeys: String*) = 
    {
        val model = toModel
        val conditions = table.columns.flatMap(column => 
        {
            if (limitKeys.isEmpty || limitKeys.exists { column.name.equalsIgnoreCase })
            {
                val value = model(column.name)
                if (value.isEmpty) None else Some(column <=> value)
            }
            else 
            {
                None
            }
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
            index
        }
        else if (table.usesAutoIncrement) 
        {
            connection(Insert(table, toModel)).generatedKeys.headOption.getOrElse(Value.empty())
        }
        else if (table.primaryColumn.isEmpty)
        {
            // If the table doesn't have an index, just inserts every time
            connection(Insert(table, toModel))
            Value.empty()
        }
        else 
        {
            Value.empty()
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
        val originalModel = if (writeNulls) toModel else toModel.withoutEmptyValues
        val updateModel = if (writeIndex || primaryColumn.isEmpty) 
                originalModel else originalModel - primaryColumn.get.name
        Update(table, updateModel)
    }
    
    /**
     * Updates certain properties to the database
     * @param propertyNames the names of the properties that are updated / pushed to the database
     * @return whether any update was performed
     */
    def updateProperties(propertyNames: Traversable[String])(implicit connection: Connection) = 
    {
        val index = this.index
        if (index.isDefined)
        {
            val update = updateStatementForProperties(propertyNames)
            if (update.isEmpty)
            {
                false
            }
            else 
            {
                connection(update + Where(table.primaryColumn.get <=> index))
                true
            }
        }
        else
        {
            false
        }
    }
    
    /**
     * Updates certain properties to the database
     * @return whether any update was performed
     */
    def updateProperties(name1: String, more: String*)(implicit connection: Connection): Boolean = 
            updateProperties(Vector(name1) ++ more);
    
    /**
     * Creates an update statement that updates only the specified properties
     * @param propertyNames the names of the properties that will be included in the update segment
     */
    def updateStatementForProperties(propertyNames: Traversable[String]) = 
    {
        def updatedProperties = valueProperties.filter { case (name, _) => 
                propertyNames.exists(name.equalsIgnoreCase) }
        Update(table, Model(updatedProperties))
    }
    
    /**
     * Creates an update statement that updates only the specified properties
     */
    def updateStatementForProperties(name1: String, more: String*): SqlSegment = 
            updateStatementForProperties(Vector(name1) ++ more);
    
    /**
     * Pushes storable data to the database, but will always insert the instance as a new row 
     * instead of updating an existing row. This will only work for tables that use auto-increment 
     * indexing or no indexing at all.
     * @return The generated index, if an insertion was made and one was generated. Empty value otherwise.
     */
    def insert()(implicit connection: Connection) = 
    {
        val primaryColumn = table.primaryColumn
        // Only works with tables that use auto-increment indexing or no indices at all
        if (primaryColumn.isDefined)
        {
            if (table.usesAutoIncrement)
            {
                connection(Insert(table, toModel - primaryColumn.get.name)
                        ).generatedKeys.headOption.getOrElse(Value.empty(primaryColumn.get.dataType))
            }
            else 
            {
                Value.empty(primaryColumn.get.dataType)
            }
        }
        else
        {
            connection(Insert(table, toModel))
            Value.empty()
        }
    }
}