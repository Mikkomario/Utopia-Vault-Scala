package utopia.vault.test

import java.time.Instant

import utopia.flow.datastructure.template.Model
import utopia.flow.datastructure.template.Property
import utopia.flow.generic.ValueConversions._
import utopia.flow.util.Equatable
import utopia.vault.model.immutable.{Storable, StorableFactory}

object Person extends StorableFactory[Person]
{
    // ATTRIBUTES    ----------------
    
    override val table = TestTables.person
    
    
    // IMPLEMENTED METHODS    -------
    
    override def apply(model: Model[Property]) = Some(new Person(model("name").stringOr(), 
            model("age").int, model("isAdmin").booleanOr(), model("created").instantOr(Instant.now()), 
            model("rowId").int))
}

/**
 * This is a test model class that implements the storable trait
 * @author Mikko Hilpinen
 * @since 18.6.2017
 */
class Person(val name: String, val age: Option[Int] = None, val isAdmin: Boolean = false, 
        val created: Instant = Instant.now(), val rowId: Option[Int] = None) extends Storable with Equatable
{
    // COMPUTED PROPERTIES    ------------------
    
    override def table = Person.table
    
    override def valueProperties = Vector("name" -> name, "age" -> age, "isAdmin" -> isAdmin, 
            "created" -> created, "rowId" -> rowId)
    
    override def properties = Vector(name, age, isAdmin, created, rowId)
}