package utopia.vault.database

import utopia.vault.generic.Table
import utopia.vault.generic.Column
import scala.collection.immutable.HashSet

/**
 * This object is used for generating select sql segments. If you wish to select all columns from a 
 * table, it is better to use SelectAll.
 * @author Mikko Hilpinen
 * @since 25.5.2017
 */
object Select
{
    // OPERATORS    -----------------
    
    /**
     * Creates an sql segment that selects a number of columns from a table
     */
    def apply(table: Table, columns: Seq[Column]) = SqlSegment(s"SELECT ${ 
            if (columns.isEmpty) "NULL" else columns.view.map { 
            _.columnNameWithTable }.reduceLeft { _ + ", " + _ } } FROM ${ table.name }", Vector(), 
            Some(table.databaseName), HashSet(table), true);
    
    /**
     * Creates an sql segment that selects a single column from a table
     */
    def apply(table: Table, column: Column): SqlSegment = apply(table, Vector(column))
    
    /**
     * Creates an sql segment that selects a number of columns from a table
     */
    def apply(table: Table, first: Column, second: Column, more: Column*): SqlSegment = 
            apply(table, Vector(first, second) ++ more);
    
    /**
     * Creates an sql segment that selects a column with a matching property name from a table
     */
    def apply(table: Table, propertyName: String): SqlSegment = apply(table, table(propertyName).toSeq);
    
    /**
     * Creates an sql segment that selects multiple columns matching property names from a table
     */
    def apply(table: Table, first: String, second: String, more: String*): SqlSegment = 
            apply(table, (Vector(first, second) ++ more).flatMap { table(_) });
    
    
    // OTHER METHODS    -------------
    
    /**
     * Creates an sql segment that selects the primary key of a table
     */
    def index(table: Table) = apply(table, table.primaryColumn.toSeq)
    
    /**
     * Creates an sql segment that selects nothing from a table
     */
    def nothing(table: Table) = apply(table, Vector())
}