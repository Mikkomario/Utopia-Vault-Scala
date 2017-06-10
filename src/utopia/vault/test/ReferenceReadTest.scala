package utopia.vault.test

import utopia.flow.generic.DataType
import utopia.vault.database.DatabaseReferenceReader
import scala.collection.immutable.HashSet
import utopia.vault.database.Connection

/**
 * This test makes sure the references can be read from the database
 * @author Mikko Hilpinen
 * @since 9.6.2017
 */
object ReferenceReadTest extends App
{
    DataType.setup()
    
    Connection.tryTransaction( implicit connection => 
    {
        val readReferences = DatabaseReferenceReader(HashSet(TestTables.person, TestTables.strength))
        
        assert(readReferences.size == 1)
        assert(readReferences.head._1 == TestTables.strength)
        assert(readReferences.head._3 == TestTables.person)
        assert(readReferences.head._2 == TestTables.strength("ownerId"))
        assert(readReferences.head._4 == TestTables.person.primaryColumn.get)
        
        println("Success!")
    })
}