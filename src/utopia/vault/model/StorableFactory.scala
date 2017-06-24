package utopia.vault.model

import utopia.flow.datastructure.template.Model
import utopia.flow.datastructure.template.Property
import utopia.flow.datastructure.immutable.Value
import utopia.vault.database.Connection
import utopia.vault.sql.SelectAll
import utopia.vault.sql.Where
import utopia.vault.sql.Condition
import utopia.vault.sql.Limit
import utopia.flow.generic.FromModelFactory

/**
 * These factory instances are used for converting database-originated model data into a 
 * storable instance.
 * @author Mikko Hilpinen
 * @since 18.6.2017
 */
trait StorableFactory[T] extends FromModelFactory[T]
{
    // ABSTRACT METHODS    --------------------
    
    /**
     * The table which resembles the generated storable structure
     */
    def table: Table
    
    
    // OTHER METHODS    -----------------------
    
    /**
     * Retrieves an object's data from the database and parses it to a proper instance
     * @param index the index / primary key with which the data is read
     * @return database data parsed into an instance. None if there was no data available.
     */
    def get(index: Value)(implicit connection: Connection): Option[T] = 
    {
        table.primaryColumn.flatMap { column => get(column <=> index) }
    }
    
    /**
     * Retrieves an object's data from the database and parses it to a proper instance
     * @param where The condition with which the row is found from the database (will be limited to 
     * the first result row)
     * @return database data parsed into an instance. None if no data was found with the provided 
     * condition
     */
    def get(where: Condition)(implicit connection: Connection) = 
    {
        connection(SelectAll(table) + Where(where) + Limit(1)).rows.headOption.map { _.toModel }.flatMap(apply)
    }
}