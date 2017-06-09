package utopia.vault.database

import utopia.vault.model.Table
import utopia.vault.model.Column
import utopia.flow.generic.AnyType
import utopia.flow.datastructure.immutable.Value

/**
 * This object is able to read table / column data from the database itself
 * @author Mikko Hilpinen
 * @since 4.6.2017
 */
object DatabaseTableReader
{
    /**
     * Reads table data from the database
     * @param databaseName the name of the database the table is read from
     * @param tableName the name of the table that is read
     * @param connection the database connection that is used
     * @param columnNameToPropertyName a function that maps column names to property names. By 
     * default, converts all names with underscores and / or whitespaces to camel case 
     * (Eg. day_of_birth => dayOfBirth)
     */
    def apply(databaseName: String, tableName: String, connection: Connection, 
            columnNameToPropertyName: String => String = underlineToCamelCase) = 
    {
        // Reads the column data from the database
        connection.dbName = databaseName
        val columnData = connection.executeQuery("DESCRIBE " + tableName)
        
        val columns = columnData.map( data => 
        {
            val columnName = data("Field")
            val isPrimary = "pri" == data("Key").toLowerCase
            val usesAutoIncrement = "auto_increment" == data("Extra").toLowerCase
            val dataType = SqlTypeInterpreterManager(data("Type")).getOrElse(AnyType)
            // val nullAllowed = "yes" == data("Null").toLowerCase
            
            val defaultString = data("Default")
            val defaultValue = if (defaultString.toLowerCase == "null" || 
                    defaultString.toLowerCase == "current_timestamp") None 
                    else Value.of(defaultString).castTo(dataType);
            
            new Column(columnNameToPropertyName(columnName), columnName, tableName, dataType, 
                    defaultValue, isPrimary, usesAutoIncrement)
        } )
        
        new Table(tableName, databaseName, columns)
    }
    
    // Converts underscrore naming style strings to camelcase naming style strings
    // Eg. day_of_birth => dayOfBirth
    private def underlineToCamelCase(original: String) = 
    {
        // whitespaces are considered equal to underscrores, in case someone would use them
        val splits = original.split(" ").flatMap { _.split("_") }
        splits.tail.foldLeft(splits.head){ _ + _.capitalize }
    }
}