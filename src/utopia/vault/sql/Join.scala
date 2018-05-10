package utopia.vault.sql

import utopia.vault.model.Column
import utopia.vault.model.Table

import utopia.vault.sql.JoinType._
import scala.collection.immutable.HashSet

/**
 * This object is used for creating join sql segments that allow selection and manipulation of 
 * multiple tables at once.
 * @author Mikko Hilpinen
 * @since 30.5.2017
 */
case class Join(val leftColumn: Column, val rightTable: Table, val rightColumn: Column, val joinType: JoinType = Inner)
{
    // COMPUTED PROPERTIES    ----------------
    
    def toSqlSegment = SqlSegment(s"$joinType JOIN $rightTable ON ${ 
            leftColumn.columnNameWithTable } = ${ rightColumn.columnNameWithTable }", Vector(), 
            Some(rightTable.databaseName), HashSet(rightTable))
}