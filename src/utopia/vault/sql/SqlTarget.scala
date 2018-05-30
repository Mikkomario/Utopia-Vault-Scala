package utopia.vault.sql

import utopia.vault.model.Table
import utopia.vault.model.References
import utopia.vault.model.Column

import utopia.vault.sql.JoinType._

/**
 * Sql targets are suited targets for operations like select, update and delete. A target may 
 * consist of one or more tables and can always be converted to an sql segment when necessary
 */
trait SqlTarget
{
    // ABSTRACT METHODS    --------------------
    
    /**
     * Converts this sql target into an sql segment
     */
    def toSqlSegment: SqlSegment
    
    
    // OPERATORS    ----------------------------
    
    /**
     * Joins another table to this target using by appending an already complete join
     */
    def +(join: Join): SqlTarget = SqlTargetWrapper(toSqlSegment + join.toSqlSegment)
    
    
    // OTHER METHODS    ------------------------
    
    /**
     * Joins another table into this sql target, if possible. The implementation searches for 
     * references between the tables and creates a join for the first find
     */
    def join(table: Table, joinType: JoinType = Inner) = 
    {
        // Finds the first table referencing (or being referenced by) the provided table and uses 
        // that for a join
        toSqlSegment.targetTables.view.map(left => References.columnsBetween(left, table)).find(
                !_.isEmpty).map(matches => this + Join(matches.head._1, table, matches.head._2, 
                joinType)).getOrElse(this)
    }
    
    /**
     * Joins another table into this sql target based on a reference in the provided column. 
     * This will only work if the column belongs to one of the already targeted tables and 
     * the column references another column
     */
    def joinFrom(column: Column, joinType: JoinType = Inner) = 
    {
        toSqlSegment.targetTables.find(_.contains(column)).flatMap(References.from(_, column)).map(
                target => this + Join(column, target.table, target.column, joinType)).getOrElse(this)
    }
}