package utopia.vault.test

import utopia.flow.generic.DataType
import utopia.vault.database.Connection
import utopia.vault.sql.Delete
import utopia.flow.datastructure.immutable.Model
import utopia.flow.datastructure.immutable.Value
import utopia.vault.sql.Insert

import utopia.vault.sql.JoinType._
import utopia.vault.sql.Select
import utopia.vault.sql.SelectAll
import utopia.vault.sql.Where

/**
 * This test tests the use of joined sql targets and the use of references too. It is expected 
 * that SimpleStatementTest and ColumnConditionTest run successfully before attempting this test.
 * @author Mikko Hilpinen
 * @since 3.6.2017
 */
object JoinTest extends App
{
    DataType.setup()
    
    val person = TestTables.person
    val strength = TestTables.strength
    
    // Uses a single connection throughout the tests
    val connection = new Connection()
    try
    {
        connection(Delete(person))
        
        // Inserts test persons
        val arttu = Model(Vector(("name", Value of "Arttu"), ("age", Value of 16)))
        val bertta = Model(Vector(("name", Value of "Bertta"), ("age", Value of 8)))
        val camilla = Model(Vector(("name", Value of "Camilla"), ("age", Value of 31)))
        
        val arttuId = connection(Insert(person, arttu)).generatedKeys.head
        val berttaId = connection(Insert(person, bertta)).generatedKeys.head
        val camillaId = connection(Insert(person, camilla)).generatedKeys.head
        
        // Adds test powers
        val berttaPower = Model(Vector(("ownerId", Value of berttaId), 
                ("name", Value of "imagination"), ("powerLevel", Value of 9999)));
        val camillaPower1 = Model(Vector(("ownerId", Value of camillaId), ("name", Value of "is teacher")));
        val camillaPower2 = Model(Vector(("ownerId", Value of camillaId), ("name", Value of "discipline"), 
                ("powerLevel", Value of 172)));
        val camillaPower3 = Model(Vector(("ownerId", Value of camillaId), ("name", Value of "imagination"), 
                ("powerLevel", Value of 250)))
        
        connection(Insert(strength, berttaPower, camillaPower1, camillaPower2, camillaPower3))
        
        // Counts the number of rows on each join type
        def countRows(joinType: JoinType) = connection(Select.nothing(person.join(strength, joinType))).rows.size
        
        assert(countRows(Left) == 5)
        assert(countRows(Right) == 4)
        assert(countRows(Inner) == 4)
        
        // Tries retrieving row data with a conditional select
        val result1 = connection(SelectAll(person join strength) + 
                Where(strength("name") <=> Value.of("discipline")));
        
        assert(result1.rows.size == 1)
        assert(result1.rows.head(person)("rowId") == Value.of(camillaId))
        assert(result1.rows.head(strength)("name") == Value.of("discipline"))
        
        def powersForPerson(personName: String) = connection(
                Select(person join strength, strength.columns) + 
                Where(person("name") <=> Value.of(personName)));
        
        assert(powersForPerson("Arttu").isEmpty)
        assert(powersForPerson("Camilla").rows.size == 3)
        assert(powersForPerson("Bertta").rows.head("name") == Value.of("imagination"))
        
        println("Success!")
    }
    finally
    {
        connection.close()
    }
}