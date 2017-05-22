package utopia.vault.test

import utopia.vault.generic.Table
import utopia.vault.generic.Column
import utopia.flow.generic.IntType
import utopia.flow.generic.StringType
import utopia.flow.generic.BooleanType
import utopia.flow.generic.InstantType
import utopia.flow.datastructure.immutable.Value

/**
 * This is a collection of tables used in the tests
 */
object TestTables
{
    val person = new Table("person", "vault_test", Vector(
            new Column("rowId", "row_id", IntType, true, true, true), 
            new Column("name", "name", StringType, true), 
            new Column("age", "age", IntType), 
            new Column("isAdmin", "is_admin", BooleanType, true, false, false, Some(Value of false)), 
            new Column("created", "created", InstantType, true)))
}