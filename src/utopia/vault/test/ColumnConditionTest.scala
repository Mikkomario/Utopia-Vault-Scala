package utopia.vault.test

import utopia.flow.generic.DataType
import utopia.vault.database.Connection
import utopia.vault.sql.Delete
import utopia.flow.datastructure.immutable.Value
import utopia.flow.datastructure.immutable.Model
import utopia.vault.sql.Insert
import utopia.vault.sql.Condition
import utopia.vault.sql.SelectAll
import utopia.vault.sql.Where

import utopia.vault.sql.Extensions._

/**
 * This test makes sure basic column-generated conditions are working properly. RawStatement-, 
 * Insert- and SimpleStatement tests should succeed before attempting this one
 * @author Mikko Hilpinen
 * @since 23.5.2017
 */
object ColumnConditionTest extends App
{
    DataType.setup()
    
    val table = TestTables.person
    
    val connection = new Connection()
    try
    {
        // Empties the table, like usually
        connection(Delete(table))
        
        // Inserts various rows
        val test1 = Model(Vector(("name", Value of "test 1"), ("age", Value of 31), ("isAdmin", Value of true)))
        val test2 = Model(Vector(("name", Value of "test 2"), ("age", Value of 32)))
        val test3 = Model(Vector(("name", Value of "test 3"), ("age", Value of 3)))
        val test4 = Model(Vector(("name", Value of "test 4")))
        
        connection(Insert(table, test1, test2, test3, test4))
        
        def countRows(condition: Condition) = connection(SelectAll(table) + Where(condition)).rows.size
        
        val isAdminColumn = table("isAdmin").get
        assert(countRows(isAdminColumn <=> Value.of(true)) == 1)
        
        val ageColumn = table("age").get
        assert(countRows(ageColumn > Value.of(31)) == 1)
        assert(countRows(ageColumn >= Value.of(31)) == 2)
        assert(countRows(ageColumn < Value.of(31)) == 1)
        assert(countRows(ageColumn <= Value.of(31)) == 2)
        assert(countRows(ageColumn <=> Value.of(31)) == 1)
        assert(countRows(ageColumn <> Value.of(31)) == 2)
        
        assert(countRows(ageColumn.isNull) == 1)
        assert(countRows(ageColumn <=> Value.empty()) == 1)
        assert(countRows(ageColumn.isNotNull) == 3)
        assert(countRows(ageColumn <> Value.empty()) == 3)
        
        assert(countRows(isAdminColumn <=> Value.of(true) || (ageColumn < Value.of(5))) == 2)
        assert(countRows(ageColumn > Value.of(5) && (ageColumn < Value.of(32))) == 1)
        assert(countRows(!ageColumn.isNull) == 3)
        
        assert(countRows(ageColumn.in(Vector(Value.of(31), Value.of(32)))) == 2)
        assert(countRows(ageColumn.isBetween(Value of 1, Value of 31)) == 2)
        
        println("Success!")
    }
    finally
    {
        connection.close()
    }
}