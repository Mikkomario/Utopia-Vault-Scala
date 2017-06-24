package utopia.vault.test

import utopia.vault.model.Table
import utopia.vault.model.Column
import utopia.flow.generic.IntType
import utopia.flow.generic.StringType
import utopia.flow.generic.BooleanType
import utopia.flow.generic.InstantType
import utopia.flow.datastructure.immutable.Value
import utopia.vault.model.References
import scala.collection.immutable.HashSet
import utopia.flow.generic.ValueConversions._

/**
 * This is a collection of tables used in the tests
 */
object TestTables
{
    // TODO: Make it so that default values are generated only when asked?
    val person = new Table("person", "vault_test", Vector(
            new Column("rowId", "row_id", "person", IntType, None, true, true), 
            new Column("name", "name", "person", StringType), 
            new Column("age", "age", "person", IntType), 
            new Column("isAdmin", "is_admin", "person", BooleanType, Some(false)), 
            new Column("created", "created", "person", InstantType)))
    
    val strength = new Table("strength", "vault_test", Vector(
            new Column("rowId", "row_id", "strength", IntType, None, true, true), 
            new Column("ownerId", "owner_id", "strength", IntType), 
            new Column("name", "name", "strength", StringType), 
            new Column("powerLevel", "power_level", "strength", IntType)))
    
    // Also records references
    References.setup((strength, "ownerId", person, "rowId"))
}