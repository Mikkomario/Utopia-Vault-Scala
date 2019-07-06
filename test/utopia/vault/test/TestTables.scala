package utopia.vault.test

import utopia.flow.generic.IntType
import utopia.flow.generic.StringType
import utopia.flow.generic.BooleanType
import utopia.flow.generic.InstantType

import utopia.flow.generic.ValueConversions._
import utopia.vault.model.immutable.{Column, Table}
import utopia.vault.model.mutable.References

/**
 * This is a collection of tables used in the tests
 */
object TestTables
{
    // TODO: Make it so that default values are generated only when asked?
    val person = Table("person", "vault_test", Vector(
            Column("rowId", "row_id", "person", IntType, false, None, true, true),
            Column("name", "name", "person", StringType, false),
            Column("age", "age", "person", IntType),
            Column("isAdmin", "is_admin", "person", BooleanType, true, Some(false)),
            Column("created", "created", "person", InstantType, false)))
    
    val strength = Table("strength", "vault_test", Vector(
            Column("rowId", "row_id", "strength", IntType, false, None, true, true),
            Column("ownerId", "owner_id", "strength", IntType, false),
            Column("name", "name", "strength", StringType, false),
            Column("powerLevel", "power_level", "strength", IntType)))
    
    // Also records references
    References.setup((strength, "ownerId", person, "rowId"))
}