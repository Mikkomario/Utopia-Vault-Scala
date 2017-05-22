package utopia.vault.test

import utopia.flow.generic.DataType
import utopia.vault.database.Connection
import utopia.vault.database.Insert
import utopia.flow.datastructure.immutable.Model
import utopia.flow.datastructure.immutable.Constant
import utopia.flow.datastructure.immutable.Value

/**
 * This is a test for the use of the insert statement. RawStatementTest should succeed before 
 * this can give valid results.
 * @author Mikko Hilpinen
 * @since 22.5.2017
 */
object InsertTest extends App
{
    DataType.setup()
    
    val table = TestTables.person
    
    // Uses a single connection throughout the test
    val connection = new Connection(Some(table.databaseName))
    try
    {
        // Removes any existing data
        connection.execute(s"DELETE FROM ${table.name}")
        
        // Inserts some simple cases
        val arttu = Model(Vector(("name", Value of "Arttu"), ("age", Value of 18)))
        val belinda = Model(Vector(("name", Value of "Belinda"), ("age", Value of 22)))
        
        val insert1 = Insert(table, Vector(arttu, belinda))
        println(insert1.description)
        assert(connection(insert1).generatedKeys.size == 2)
        
        // Inserts cases where all values are not provided
        val cecilia = Model(Vector(("name", Value of "Cecilia"), ("age", Value of 25)))
        val daavid = Model(Vector(("name", Value of "Daavid")))
        
        assert(connection(Insert(table, Vector(cecilia, daavid))).generatedKeys.size == 2)
        
        // Inserts a row that would include a row id
        val elias = Model(Vector(("name", Value of "Elias"), ("rowId", Value of 22)))
        assert(connection(Insert(table, elias)).generatedKeys.size == 1)
        
        println("Success!")
    }
    finally
    {
        connection.close()
    }
}