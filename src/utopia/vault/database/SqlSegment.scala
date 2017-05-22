package utopia.vault.database

import utopia.flow.datastructure.immutable.Value
import utopia.vault.generic.Table
import scala.collection.immutable.HashSet

/**
 * Sql Segments can be combined to form sql statements. Some may contain value assignments too.
 * @author Mikko Hilpinen
 * @since 12.3.2017
 * @param sql The sql string representing the segment. Each of the segment's values is indicated with a 
 * '?' character.
 * @param values The values that will be inserted to this segment when it is used. Each '?' in the sql will 
 * be replaced with a single value. Empty values will be interpreted as NULL.
 */
case class SqlSegment(val sql: String, val values: Vector[Value] = Vector(), 
        val databaseName: Option[String] = None, val selectedTables: Set[Table] = HashSet(), 
        val generatesKeys: Boolean = false)
{
    // OPERATORS    ---------------------
    
    /**
     * Combines two sql segments to create a single, larger segment. A whitespace character is 
     * added between the two sql segments.
     */
    def +(other: SqlSegment) = SqlSegment(sql + " " + other.sql, values ++ other.values, 
            databaseName orElse other.databaseName, selectedTables ++ other.selectedTables, 
            generatesKeys || other.generatesKeys)
}