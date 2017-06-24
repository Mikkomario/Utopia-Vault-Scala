package utopia.vault.sql

import utopia.flow.datastructure.immutable.Value
import utopia.vault.model.Table
import scala.collection.immutable.HashSet
import scala.Vector

object SqlSegment
{
    /**
     * An empty sql segment. Some functions may return this in case of no-op
     */
    val empty = SqlSegment("")
    
    /**
     * Combines a number of sql segments together, forming a single longer sql segment
     * @param segments the segments that are combined
     * @param sqlReduce the reduce function used for appending the sql strings together. By default 
     * just adds a whitespace between the strings
     */
    def combine(segments: Seq[SqlSegment], sqlReduce: (String, String) => String = { _ + " " + _ }) = 
    {
        val sql = segments.view.map { _.sql }.reduceLeft(sqlReduce)
        val databaseName = segments.view.flatMap { _.databaseName }.headOption
        
        SqlSegment(sql, segments.flatMap { _.values }, databaseName, 
                segments.flatMap { _.targetTables }.toSet, segments.exists { _.isSelect }, 
                segments.exists { _.generatesKeys })
    }
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
case class SqlSegment(val sql: String, val values: Seq[Value] = Vector(), 
        val databaseName: Option[String] = None, val targetTables: Set[Table] = HashSet(), 
        val isSelect: Boolean = false, val generatesKeys: Boolean = false)
{
    // COMPUTED PROPERTIES    -----------
    
    override def toString = sql
    
    /**
     * A textual description of the seqment. Contains the sql string as well as the included values
     */
    def description = s"sql: $toString\nvalues: ${values.map { _.description }.reduce(_ + ", " + _)}"
    
    /**
     * Whether the segment is considered to be empty (no-op)
     */
    def isEmpty = sql.isEmpty()
    
    
    // OPERATORS    ---------------------
    
    /**
     * Combines two sql segments to create a single, larger segment. A whitespace character is 
     * added between the two sql segments.
     */
    def +(other: SqlSegment) = SqlSegment(sql + " " + other.sql, values ++ other.values, 
            databaseName orElse other.databaseName, targetTables ++ other.targetTables, 
            isSelect || other.isSelect, generatesKeys || other.generatesKeys);
    
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