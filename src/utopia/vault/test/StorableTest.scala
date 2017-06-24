package utopia.vault.test

import utopia.flow.generic.DataType
import utopia.vault.database.Connection
import utopia.vault.sql.Delete

import utopia.flow.generic.ValueConversions._
import utopia.flow.datastructure.immutable.Value
import utopia.vault.sql.Select
import java.time.Instant

/**
 * This test tests the use of a simple storable trait implementation
 * @author Mikko Hilpinen
 * @since 24.6.2017
 */
object StorableTest extends App
{
    DataType.setup()
    
    Connection.doTransaction
    { implicit connection => 
        
        // Deletes data from previous tests
        connection(Delete(TestTables.person))
        
        // Inserts a new instance
        val arttu = new Person("Arttu", Some(21), true)
        val arttuId = arttu.insert()
        
        assert(arttuId.isDefined)
        
        println(arttu)
        
        // Reads data from the database
        val readArttu = Person.get(arttuId).get
        
        println(readArttu)
        
        assert(readArttu.rowId == arttuId.int)
        assert(readArttu.name == arttu.name)
        assert(readArttu.isAdmin == arttu.isAdmin)
        assert(readArttu.age == arttu.age)
        
        val readArttu2 = Person.get(arttu.toCondition("name").get)
        
        assert(readArttu2.isDefined)
        assert(readArttu2.get == readArttu)
        
        // Updates some of the database data
        val updateModel = new Person(readArttu.name, Some(22), rowId = arttuId.int)
        
        assert(updateModel.updateProperties("age"))
        
        val readArttu3 = Person.get(arttuId).get
        
        assert(readArttu3.age == updateModel.age)
        
        assert(connection(Select.nothing(TestTables.person)).rows.size == 1)
        
        val belinda = new Person("Belinda")
        val belindaId = belinda.push()
        
        assert(belindaId.isDefined)
        assert(connection(Select.nothing(TestTables.person)).rows.size == 2)
        
        val belindaUpdateModel = new Person("Belinda", Some(19), true, rowId = belindaId.int)
        
        assert(belindaUpdateModel.push() == belindaId)
        assert(connection(Select.nothing(TestTables.person)).rows.size == 2)
        
        val allPersons = Person.getAll()
        
        assert(allPersons.size == 2)
        println()
        allPersons.foreach(println)
        
        println("Success!")
    }
}