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
import java.time.Instant
import utopia.flow.generic.VectorType
import scala.collection.immutable.HashSet

/**
 * This test runs some raw statements using the sql client and checks the results
 * @since 19.5.2017
 */
object RawStatementTest extends App
{
    // Sets up the data types
    DataType.setup()
    
    // Creates the test table first
    val table = TestTables.person
    
    // Uses a single connection throughout the test
    val connection = new Connection(Some(table.databaseName))
    try
    {
        // Makes sure the table is empty
        connection.executeSimple(s"DELETE FROM ${table.name}")
        
        def insert(name: String, age: Int, isAdmin: Boolean = false) = 
        {
            assert(!connection.execute(s"INSERT INTO ${table.name} (name, age, is_admin) VALUES (?, ?, ?)", 
                    Vector(Value.of(name), Value.of(age), Value.of(isAdmin)), HashSet(), 
                    true).generatedKeys.isEmpty)
        }
        
        // Inserts a couple of elements into the table. Makes sure new indices are generated
        insert("Arttu", 15, true)
        insert("Belinda", 22, false)
        insert("Cecilia", 23)
        
        // Reads person data from the database
        val results = connection.execute(s"SELECT * FROM ${table.name}", Vector(), HashSet(table)).rowModels
        results.foreach { row => println(row.toJSON) }
        
        assert(results.size == 3)
        
        // Tries to insert null values
        connection.execute(s"INSERT INTO ${table.name} (name, age) VALUES (?, ?)", 
                Vector(Value.of("Test"), Value.empty(IntType)));
        
        // Also tries inserting a time value
        val creationTime = Value of Instant.now()
        val latestIndex = connection.execute(s"INSERT INTO ${table.name} (name, created) VALUES (?, ?)", 
                Vector(Value.of("Test2"), creationTime), HashSet(), true).generatedKeys.head;
        
        // Checks that the time value was preserved
        val lastResult = connection.execute(s"SELECT created FROM ${table.name} WHERE row_id = ?", 
                Vector(Value of latestIndex), HashSet(table), false).rows.head.toModel;
        
        println(lastResult.toJSON)
        println(s"Previously ${creationTime.longOr()} (${creationTime.dataType}), now ${lastResult("created").longOr()} (${lastResult("created").dataType})")
        assert(lastResult("created").longOr() == creationTime.longOr())
        
        // Tests a bit more tricky version where data types may not be correct
        connection.execute(s"INSERT INTO ${table.name} (name) VALUES (?)", Vector(Value.of(32)))
        connection.execute(s"INSERT INTO ${table.name} (name, created) VALUES (?, ?)", Vector(Value.of("Null Test"), Value.empty(VectorType)))
        
        println("Success!")
    }
    finally
    {
        connection.close()
    }
}