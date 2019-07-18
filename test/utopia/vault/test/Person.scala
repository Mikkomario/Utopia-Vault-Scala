package utopia.vault.test

import java.time.Instant

import utopia.flow.datastructure.template.Model
import utopia.flow.datastructure.template.Property
import utopia.flow.generic.ValueConversions._
import utopia.vault.model.immutable.{StorableFactory, StorableWithFactory}

import scala.util.Success

object Person extends StorableFactory[Person]
{
    // ATTRIBUTES    ----------------
    
    override val table = TestTables.person
    
    
    // IMPLEMENTED METHODS    -------
    
    override def apply(model: Model[Property]) = Success(new Person(model("name").stringOr(),
            model("age").int, model("isAdmin").booleanOr(), model("created").instantOr(Instant.now()), 
            model("rowId").int))
}

/**
 * This is a test model class that implements the storable trait
 * @author Mikko Hilpinen
 * @since 18.6.2017
 */
case class Person(name: String, age: Option[Int] = None, isAdmin: Boolean = false,
        created: Instant = Instant.now(), rowId: Option[Int] = None) extends StorableWithFactory[Person]
{
    // COMPUTED PROPERTIES    ------------------
    
    override def factory = Person
    
    override def valueProperties = Vector("name" -> name, "age" -> age, "isAdmin" -> isAdmin, 
            "created" -> created, "rowId" -> rowId)
}