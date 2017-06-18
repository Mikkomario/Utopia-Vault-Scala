package utopia.vault.test

import java.time.Instant
import utopia.vault.model.Storable
import utopia.flow.datastructure.immutable.Value
import utopia.flow.generic.IntType
import utopia.vault.model.StorableFactory
import utopia.flow.datastructure.template.Model
import utopia.flow.datastructure.template.Property

object Person extends StorableFactory[Person]
{
    // ATTRIBUTES    ----------------
    
    override val table = TestTables.person
    
    
    // IMPLEMENTED METHODS    -------
    
    override def apply(model: Model[Property]) = new Person(model("name").stringOr(), 
            model("age").int, model("isAdmin").booleanOr(), model("created").instantOr(Instant.now()), 
            model("rowId").int)
}

/**
 * This is a test model class that implements the storable trait
 * @author Mikko Hilpinen
 * @since 18.6.2017
 */
class Person(val name: String, val age: Option[Int] = None, val isAdmin: Boolean = false, 
        val created: Instant = Instant.now(), val rowId: Option[Int] = None) extends Storable
{
    // COMPUTED PROPERTIES    ------------------
    
    override def table = Person.table
    
    
    // IMPLEMENTED METHODS    ------------------
    
    override def valueForProperty(propertyName: String) = 
    {
        // TODO: A lot of repetition here. Try creating a trait and extensions so that 
        // it's something like Value.of(ValueConvertible) or Value.of(Option[ValueConvertible])
        // Also, Value.of(Seq[ValueConvertible])
        // Also, valueConvertible implicit conversion to value!
        propertyName.toLowerCase() match 
        {
            case "rowid" => rowId.map { Value.of }.getOrElse(Value.empty(IntType))
            case "name" => Value of name
            case "age" => age.map { Value.of }.getOrElse(Value.empty(IntType))
            case "isadmin" => Value of isAdmin
            case "created" => Value of created
            case _ => Value.empty()
        }
    }
}