package utopia.vault.sql

import utopia.vault.model.Table
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
     * @param rows models representing rows in the table. Only properties with values and which 
     * match those of the table are used
     * @return An insert segment. None if there was nothing to insert
     */
    def apply(table: Table, rows: Vector[Model[Property]]) = 
    {
        // Finds the inserted properties that are applicable to this table
        // Only properties matching columns (that are not auto-increment) are included
        val insertedPropertyNames = rows.flatMap { _.attributesWithValue.map { _.name } }.filter {
            table.find(_).exists { !_.usesAutoIncrement } }
        
        if (insertedPropertyNames.isEmpty)
            None
        else 
        {
            val columnNames = insertedPropertyNames.map { table(_).columnName }.mkString(", ")
            val singleValueSql = "(?" + ", ?" * (insertedPropertyNames.size - 1) + ")"
            val valuesSql = singleValueSql + (", " + singleValueSql) * (rows.size - 1)
            
            val values = rows.flatMap { model => insertedPropertyNames.map { model(_) } }
            
            Some(SqlSegment(s"INSERT INTO ${table.name} ($columnNames) VALUES $valuesSql", values, 
                    Some(table.databaseName), HashSet(table), false, table.usesAutoIncrement))
        }
    }
    
    /**
     * A convenience method for creating an insert statement for a single row.
     * @param table The table to which the row is inserted
     * @param row the row that is inserted to the table. Only properties matching table columns are
     * used
     * @return An insert segment. None if there was nothing to insert
     */
    def apply(table: Table, row: Model[Property]): Option[SqlSegment] = apply(table, Vector(row))
    
    /**
     * Creates a new statement that inserts multiple rows into an sql database. This statement is
     * generally not combined with other statements and targets a single table only
     * @param table the table into which the rows are inserted
     * @return An insert segment. None if there was nothing to insert
     */
    def apply(table: Table, first: Model[Property], second: Model[Property], 
            more: Model[Property]*): Option[SqlSegment] = apply(table, Vector(first, second) ++ more)
}