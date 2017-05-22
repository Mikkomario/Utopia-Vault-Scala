package utopia.vault.database

import utopia.vault.generic.Table
import utopia.flow.datastructure.template.Model
import utopia.flow.datastructure.template.Property
import scala.collection.immutable.HashSet

/**
 * Insert object is used for generating insert statements that can then be executed with a 
 * suitable database connection
 * @author Mikko Hilpinen
 * @since 22.5.2017
 */
object Insert
{
    /**
     * Creates a new statement that inserts multiple rows into an sql database. This statement is
     * generally not combined with other statements and targets a single table only
     * @param table the table into which the rows are inserted
     * @param rows models representing rows in the table. Only properties matching those of the
     * table are used
     */
    def apply(table: Table, rows: Vector[Model[Property]]) = 
    {
        // Finds the inserted properties that are applicable to this table
        // Only properties matching columns (that are not auto-increment) are included
        val insertedPropertyNames = rows.foldLeft(HashSet[String]()) {
                _ ++ _.attributesWithValue.map { _.name }.filter { 
                table(_).exists { !_.usesAutoIncrement } } }.toVector
        
        if (insertedPropertyNames.isEmpty)
        {
            SqlSegment.empty
        }
        else 
        {
            val columnNames = insertedPropertyNames.map { table(_).get.columnName }.reduce(_ + ", " + _)
            val singleValueSql = "(?" + ", ?" * (insertedPropertyNames.size - 1) + ")"
            val valuesSql = singleValueSql + (", " + singleValueSql) * (rows.size - 1)
            
            val values = rows.flatMap { model => insertedPropertyNames.map { model(_) } }
            
            SqlSegment(s"INSERT INTO ${table.name} ($columnNames) VALUES $valuesSql", values, 
                    Some(table.databaseName), HashSet(), table.usesAutoIncrement)
        }
    }
    
    /**
     * A convenience method for creating an insert statement for a single row.
     * @param table The table to which the row is inserted
     * @param row the row that is inserted to the table. Only properties matching table columns are
     * used
     */
    def apply(table: Table, row: Model[Property]): SqlSegment = apply(table, Vector(row))
}