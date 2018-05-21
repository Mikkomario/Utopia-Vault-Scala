package utopia.vault.database

import utopia.flow.generic.ValueConversions._
import utopia.vault.sql.Extensions._

import utopia.vault.model.Table
import utopia.vault.model.Column
import utopia.flow.generic.StringType
import utopia.vault.sql.Select
import utopia.vault.sql.Where
import utopia.flow.datastructure.immutable.Value

import utopia.vault.sql.Extensions._
import utopia.vault.sql.ConditionElement
import utopia.flow.datastructure.template.Model
import utopia.flow.datastructure.template.Property
import utopia.vault.model.References
import utopia.vault.sql.ConditionValue
import utopia.vault.model.Reference

/**
 * This object can be used for reading and setting up table references by reading them directly 
 * from the database.
 * @author Mikko Hilpinen
 * @since 9.6.2017
 */
object DatabaseReferenceReader
{
    private val keys = new Table("KEY_COLUMN_USAGE", "INFORMATION_SCHEMA", Vector(
            new Column("schema", "TABLE_SCHEMA", "KEY_COLUMN_USAGE", StringType, false), 
            new Column("tableName", "TABLE_NAME", "KEY_COLUMN_USAGE", StringType, true), 
            new Column("columnName", "COLUMN_NAME", "KEY_COLUMN_USAGE", StringType, true), 
            new Column("referencedTableName", "REFERENCED_TABLE_NAME", "KEY_COLUMN_USAGE", StringType, true), 
            new Column("referencedColumnName", "REFERENCED_COLUMN_NAME", "KEY_COLUMN_USAGE", StringType, true)))
    
    /**
     * Reads all references between the provided tables
     * @param tables the tables for which the references are read. All tables should belong to the 
     * same database
     * @param connection the database connection that is used
     */
    def apply(tables: Set[Table])(implicit connection: Connection) = 
    {
        if (tables.isEmpty)
        {
            Vector()
        }
        else 
        {
            val databaseName = tables.head.databaseName
            val tableOptions = tables.map(_.name: ConditionElement).toSeq
            val results = connection(Select(keys, keys.columns) + Where(
                    keys("schema") <=> databaseName && 
                    keys("tableName").in(tableOptions) && 
                    keys("referencedTableName").in(tableOptions))).rows.map(_.toModel)
            
            def findTable(keyName: String, row: Model[Property]) = tables.find(
                    _.name == row(keyName).stringOr()).get
            def findColumn(table: Table, keyName: String, row: Model[Property]) = 
                    table.columnWithColumnName(row(keyName).stringOr())
            
            results.map( row => 
            {
                val sourceTable = findTable("tableName", row)
                val sourceColumn = findColumn(sourceTable, "columnName", row)
                val targetTable = findTable("referencedTableName", row)
                val targetColumn = findColumn(targetTable, "referencedColumnName", row)
                
                Reference(sourceTable, sourceColumn, targetTable, targetColumn)
            } )
        }
    }
    
    /**
     * Sets up the References object to contain all references between the provided tables. If 
     * there are tables from multiple databases, references are set up for all of them.
     * @param tables the tables between which the references are searched. Should contain all 
     * tables for each included database
     * @param connection the database connection used
     */
    def setupReferences(tables: Set[Table])(implicit connection: Connection) = 
    {
        val tablesForDatabase = tables.groupBy(_.databaseName)
        tablesForDatabase.foreach { case (dbName, tables) => 
                References.setup(dbName, apply(tables)(connection).toSet) }
    }
}