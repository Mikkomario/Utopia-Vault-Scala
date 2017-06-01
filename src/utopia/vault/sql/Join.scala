package utopia.vault.sql

import utopia.vault.model.Column
import utopia.vault.model.Table

import utopia.vault.sql.JoinType._
import scala.collection.immutable.HashSet

/**
 * This object is used for creating join sql segments that allow selection and manipulation of 
 * multiple tables at once. Some of the methods in this object use the References -interface, 
 * which should be properly set up before it is used.
 * @author Mikko Hilpinen
 * @since 30.5.2017
 */
object Join
{
    // TODO: Make this a class and not an object. It doesn't need to be an sql segment, just convertible to one
    
    // TODO: Create a target segment trait that allows one to join more targets
    // I guess it would need rightMost table reference or something - or just use the ones on the 
    // segment. Join and table can then implement this
    
    def apply(leftColumn: Column, rightTable: Table, rightColumn: Column, 
            joinType: JoinType = Inner) = SqlSegment(s"$joinType JOIN $rightTable ON ${ 
            leftColumn.columnNameWithTable } = ${ rightColumn.columnNameWithTable }", Vector(), 
            Some(rightTable.databaseName), HashSet(rightTable));
    
    
}