package utopia.vault.model

import utopia.flow.util.Equatable
import utopia.flow.datastructure.immutable.ModelDeclaration
import utopia.vault.sql.SqlTarget
import utopia.vault.sql.SqlSegment
import scala.collection.immutable.HashSet

import utopia.vault.sql.JoinType._

/**
 * A table represents a table in the database. Each table has a set of columns, one of which is 
 * usually the primary column. Tables may reference other tables through columns too.
 * @author Mikko Hilpinen
 * @since 9.3.2017
 */
class Table(val name: String, val databaseName: String, val columns: Vector[Column]) extends Equatable with SqlTarget
{
    // ATTRIBUTES    ---------------------------
    
    /**
     * A model declaration based on this table
     */
    lazy val toModelDeclaration = new ModelDeclaration(columns)
    
    
    // COMPUTED PROPERTIES    ------------------
 
    override def toString = name
    
    override def properties: Vector[Any] = Vector(name, databaseName, columns)
    
    override def toSqlSegment = SqlSegment(name, Vector(), Some(databaseName), HashSet(this))
    
    /**
     * The primary column for this table. Not all tables do have primary columns though.
     */
    def primaryColumn = columns.find { _.isPrimary }
    
    /**
     * Whether the table has an index (primary key) that uses auto-increment
     */
    def usesAutoIncrement = primaryColumn.exists { _.usesAutoIncrement }
    
    
    // OPERATORS    ----------------------------
    
    /**
     * Finds a column with the provided property name associated with it. If you are unsure whether 
     * such a column exists in the table, use find instead
     */
    def apply(propertyName: String) = find(propertyName).get
    
    /**
     * Finds the columns matching the provided property names
     */
    def apply(propertyNames: Traversable[String]) = columns.filter { 
            column => propertyNames.exists { _ == column.name } }
    
    /**
     * Finds columns matching the provided property names
     */
    def apply(propertyName: String, others: String*): Vector[Column] = apply(others :+ propertyName)
    
    
    // OTHER METHODS    ------------------------
    
    /**
     * Finds a column with the provided property name. Returns None if no such column exists in 
     * the table
     */
    def find(propertyName: String) = columns.find { _.name == propertyName }
    
    /**
     * Finds a column with the specified column name. Returns None if no such column exists.
     */
    def findColumnWithColumnName(columnName: String) = columns.find { _.columnName == columnName }
    
    /**
     * Finds a column with the specified column name. If you are unsure whether such a column 
     * exists, please used findColumnWithColumnName instead
     */
    def columnWithColumnName(columnName: String) = findColumnWithColumnName(columnName).get
    
    /**
     * Checks whether this table contains a matching column
     */
    def contains(column: Column) = columns.contains(column)
    
    /**
     * checks whether this table contains a column matching the provided property name
     */
    def contains(propertyName: String) = columns.exists(_.name == propertyName)
    
    /**
     * Joins a new table, creating a new sql target.
     * @param propertyName the name of a property matching a column in this table, which makes a 
     * reference to another table
     */
    def joinFrom(propertyName: String, joinType: JoinType): SqlTarget = 
            find(propertyName).map { joinFrom(_, joinType) }.getOrElse(this)
}