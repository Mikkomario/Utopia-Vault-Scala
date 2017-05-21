package utopia.vault.test

import utopia.vault.generic.Table
import utopia.vault.generic.Column
import utopia.flow.generic.IntType
import utopia.flow.generic.StringType
import utopia.flow.generic.BooleanType
import utopia.flow.datastructure.immutable.Value
import utopia.flow.generic.InstantType
import utopia.flow.generic.DataType
import utopia.vault.database.Connection
import utopia.vault.database.ConnectionSettings

/**
 * This test runs some raw statements using the sql client and checks the results
 * @since 19.5.2017
 */
object RawStatementTest extends App
{
    // Sets up the data types
    DataType.setup()
    
    // Creates the test table first
    val table = new Table("person", "vault_test", Vector(
            new Column("rowId", "row_id", IntType, true, true, true), 
            new Column("name", "name", StringType, true), 
            new Column("age", "age", IntType), 
            new Column("isAdmin", "is_admin", BooleanType, true, false, false, Some(Value of false)), 
            new Column("created", "created", InstantType, true)))
    
    // Uses a single connection throughout the test
    val connection = new Connection(Some(table.databaseName))
    try
    {
        // Makes sure the table is empty
        connection.executeSimple(s"DELETE FROM ${table.name}")
        
        def insert(name: String, age: Int, isAdmin: Boolean = false) = 
        {
            assert(!connection.execute(s"INSERT INTO ${table.name} (name, age, is_admin) VALUES (?, ?, ?)", 
                    Vector(Value.of(name), Value.of(age), Value.of(isAdmin)), Vector(), 
                    true).generatedKeys.isEmpty)
        }
        
        // Inserts a couple of elements into the table. Makes sure new indices are generated
        insert("Arttu", 15, true)
        insert("Belinda", 22, false)
        insert("Cecilia", 23)
        
        // Reads person data from the database
        val results = connection.execute(s"SELECT * FROM ${table.name}", Vector(), Vector(table)).rowModels
        results.foreach { row => println(row.toJSON) }
        
        assert(results.size == 3)
        
        println("Success!")
    }
    finally
    {
        connection.close()
    }
}