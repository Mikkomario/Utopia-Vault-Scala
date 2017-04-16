package utopia.vault.database

import utopia.flow.datastructure.immutable.Value

/**
 * This is a very basic implementation of the SqlSegment trait. The class contains an immutable 
 * sql string and a set of values.
 */
class SimpleSqlSegment(val sql: String, val values: Vector[Value] = Vector())


// Here are some simple Sql Statements
//object Insert extends SimpleSqlSegment("INSERT INTO")
