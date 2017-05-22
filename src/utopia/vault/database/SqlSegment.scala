package utopia.vault.database

import utopia.flow.datastructure.immutable.Value
import utopia.vault.generic.Table
import scala.collection.immutable.HashSet

object SqlSegment
{
    /**
     * An empty sql segment. Some functions may return this in case of no-op
     */
    val empty = SqlSegment("")    
}

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
    // COMPUTED PROPERTIES    -----------
    
    override def toString = sql
    
    /**
     * A textual description of the seqment. Contains the sql string as well as the included values
     */
    def description = s"sql: $toString\nvalues: ${values.map { _.description }.reduce(_ + ", " + _)}"
    
    
    // OPERATORS    ---------------------
    
    /**
     * Combines two sql segments to create a single, larger segment. A whitespace character is 
     * added between the two sql segments.
     */
    def +(other: SqlSegment) = SqlSegment(sql + " " + other.sql, values ++ other.values, 
            databaseName orElse other.databaseName, selectedTables ++ other.selectedTables, 
            generatesKeys || other.generatesKeys);
    
    /**
     * Appends this sql segment with an sql string. 
     * The new string will be added to the end of this segment. Adds a whitespace character 
     * between the two sql segments.
     */
    def +(sql: String) = copy(sql = this.sql + " " + sql)
    
    /**
     * Prepends this sql segment with an sql string. The new string will be added to the beginning 
     * of this segment. A whitespace character is added between the two segments.
     */
    def prepend(sql: String) = copy(sql = sql + " " + this.sql)
}