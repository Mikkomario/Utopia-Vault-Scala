package utopia.vault.test

import utopia.flow.generic.DataType
import utopia.vault.database.Connection
import utopia.vault.database.SelectAll
import utopia.vault.database.Delete
import utopia.flow.datastructure.immutable.Model
import utopia.flow.datastructure.immutable.Value
import utopia.vault.database.Insert

/**
 * This test tests basic uses cases for very simple statements delete and select all.
 * @author Mikko Hilpinen
 * @since 22.5.2017
 */
object SimpleStatementTest extends App
{
    DataType.setup()
    val table = TestTables.person
    
    // Uses a single connection
    val connection = new Connection()
    try
    {
        def countRows = connection(SelectAll(table)).rows.size
        
        // Empties the database and makes sure it is empty
        connection(Delete(table))
        assert(countRows == 0)
        
        val testModel = Model(Vector(("name", Value of "SimpleStatementTest")))
        connection(Insert(table, Vector(testModel, testModel, testModel)))
        
        assert(countRows == 3)
        
        val result = connection(SelectAll(table))
        assert(result.rows.head.toModel("name") == Value.of("SimpleStatementTest"))
        
        connection(Delete(table))
        assert(countRows == 0)
        
        println("Success!")
    }
    finally
    {
        connection.close()
    }
}