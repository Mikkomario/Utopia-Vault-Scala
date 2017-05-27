package utopia.vault.test

import utopia.flow.generic.DataType
import utopia.vault.database.Connection
import utopia.vault.database.SelectAll
import utopia.vault.database.Delete
import utopia.flow.datastructure.immutable.Model
import utopia.flow.datastructure.immutable.Value
import utopia.vault.database.Insert
import utopia.vault.database.Select
import utopia.vault.database.Update
import utopia.vault.database.Limit
import utopia.vault.database.OrderBy

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
        assert(connection(Select.nothing(table)).rows.size == 3)
        assert(connection(Select.nothing(table) + Limit(1)).rows.size == 1)
        
        val result = connection(SelectAll(table))
        assert(result.rows.head.toModel("name") == Value.of("SimpleStatementTest"))
        assert(connection(Select(table, table.columns)) == result)
        assert(connection(Select(table, "name")).rows.head.toModel("name") == Value.of("SimpleStatementTest"))
        
        connection(Update(table, "age", Value of 22))
        assert(connection(Select(table, "age")).rows.head.toModel("age") == Value.of(22))
        
        connection(Insert(table, Model(Vector(("name", Value of "Last"), ("age", Value of 2)))))
        assert(connection(SelectAll(table) + OrderBy(table("age").get) + Limit(1)
                ).rows.head.toModel("name") == Value.of("Last"));
        
        connection(Delete(table))
        assert(countRows == 0)
        
        println("Success!")
    }
    finally
    {
        connection.close()
    }
}