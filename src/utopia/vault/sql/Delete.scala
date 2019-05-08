package utopia.vault.sql

import utopia.vault.model.immutable.Table

/**
 * This object is used for creating sql statements which delete contents from the database. 
 * Delete statements can be used alongside join- and where clauses, too.
 * @author Mikko Hilpinen
 * @since 22.5.2017
 */
object Delete
{
    /**
     * Creates an sql segment that deletes rows from possibly multiple tables. All of the deleted 
     * tables must be included in the provided target.
     * @param target The target for the delete operation, may be a single table or join, but must 
     * contain all deleted tables.
     * @param deletedTables The tables from which rows are deleted (shouldn't be empty)
     */
    def apply(target: SqlTarget, deletedTables: Seq[Table]) = 
            if (deletedTables.isEmpty) SqlSegment.empty else SqlSegment(
            s"DELETE ${ deletedTables.drop(1).foldLeft(deletedTables.head.name) {_ + ", " + _.name } } FROM") +
            target.toSqlSegment
    
    /**
     * Creates an sql segment that deletes rows from a single table. This segment is often followed 
     * by a where clause and possibly a limit as well.
     */
    def apply(target: SqlTarget): SqlSegment = apply(target, target.toSqlSegment.targetTables.toSeq)
}