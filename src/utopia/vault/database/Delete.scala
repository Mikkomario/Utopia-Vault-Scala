package utopia.vault.database

import utopia.vault.generic.Table

/**
 * This object is used for creating sql statements which delete contents from the database. 
 * Delete statements can be used alongside join- and where clauses, too.
 * @author Mikko Hilpinen
 * @since 22.5.2017
 */
object Delete
{
    /**
     * Creates an sql segment that deletes rows from possibly multiple tables. 
     * If multiple tables are provided, this segment must be followed with a join clause.
     * @param tables The tables from which rows are deleted
     */
    def apply(tables: Seq[Table]) = if (tables.isEmpty) SqlSegment.empty else SqlSegment(
            s"DELETE ${ tables.tail.foldLeft(tables.head.name) {_ + ", " + _.name } } FROM ${ 
            tables.head.name }", Vector(), Some(tables.head.databaseName));
    
    /**
     * Creates an sql segment that deletes rows from a single table. This segment is often followed 
     * by a where clause and possibly a limit as well.
     */
    def apply(table: Table): SqlSegment = apply(Vector(table))
}