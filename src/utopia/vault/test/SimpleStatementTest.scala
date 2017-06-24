package utopia.vault.test

import utopia.flow.generic.DataType
import utopia.vault.database.Connection
import utopia.vault.sql.SelectAll
import utopia.vault.sql.Delete
import utopia.flow.datastructure.immutable.Model
import utopia.flow.datastructure.immutable.Value
import utopia.vault.sql.Insert
import utopia.vault.sql.Select
import utopia.vault.sql.Limit
import utopia.vault.sql.Update
import utopia.vault.sql.OrderBy
import utopia.flow.generic.ValueConversions._

/**
 * sqlt tests basic uses cases for very simple statements delete and select all.
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
        
        val testModel = Model(Vector("name" -> "SimpleStatementTest"))
        connection(Insert(table, Vector(testModel, testModel, testModel)))
        
        assert(countRows == 3)
        assert(connection(Select.nothing(table)).rows.size == 3)
        assert(connection(Select.nothing(table) + Limit(1)).rows.size == 1)
        
        val result = connection(SelectAll(table))
        assert(result.rows.head.toModel("name") == "SimpleStatementTest".toValue)
        assert(connection(Select(table, table.columns)) == result)
        assert(connection(Select(table, "name")).rows.head.toModel("name") == "SimpleStatementTest".toValue)
        
        connection(Update(table, "age", 22))
        assert(connection(Select(table, "age")).rows.head.toModel("age") == 22.toValue)
        
        connection(Insert(table, Model(Vector("name" -> "Last", "age" -> 2))))
        assert(connection(SelectAll(table) + OrderBy(table("age")) + Limit(1)
                ).rows.head.toModel("name") == "Last".toValue);
        
        connection(Delete(table))
        assert(countRows == 0)
        
        println("Success!")
    }
    finally
    {
        connection.close()
    }
}