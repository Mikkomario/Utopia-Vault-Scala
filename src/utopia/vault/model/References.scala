package utopia.vault.model

import scala.collection.immutable.HashMap
import utopia.flow.generic.EnvironmentNotSetupException
import scala.collection.immutable.HashSet

/**
 * The references object keeps track of all references between different tables in a multiple 
 * databases. The object is used for accessing the reference data. The actual data must be set 
 * into the object before if can be properly used
 * @author Mikko Hilpinen
 * @since 28.5.2017
 */
object References
{
    // ATTRIBUTES    ---------------------
    
    private var referenceData = HashMap[String, Set[Tuple4[Table, Column, Table, Column]]]()
    
    
    // OTHER METHODS    ------------------
    
    /**
     * Sets up reference data for a single database
     * @param databaseName the name of the database that contains these references
     * @param references All references inside the provided database. The format for each entry 
     * is as follows: 1) referencing table, 2) referencing column, 3) referenced table, 
     * 4) referenced column
     */
    def setup(databaseName: String, references: Set[Tuple4[Table, Column, Table, Column]]) = 
            referenceData += Tuple2(databaseName, references);
    
    /**
     * Finds all references that are made from the provided column
     * @param table The table that contains the column
     * @param column the referencing column
     * @return All references that start from the provided column (target table, target column)
     */
    def from(table: Table, column: Column) = 
    {
        checkIsSetup(table.databaseName)
        referenceData(table.databaseName).find { case (sourceTable, sourceColumn, _, _) => 
                sourceTable == table && sourceColumn == column }.map { 
                case (_, _, targetTable, targetColumn) => (targetTable, targetColumn) }
    }
    
    /**
     * Finds all references that are made from the provided column
     * @param table The table that contains the column
     * @param columnName the name of the referencing column
     * @return All references that start from the provided column (target table, target column)
     */
    def from(table: Table, columnName: String): Option[Tuple2[Table, Column]] = 
            table(columnName).flatMap { from(table, _) }
    
    /**
     * Finds all places where the provided column is referenced
     * @param table the table that contains the column
     * @param columnn the referenced column
     * @return All references to the specified column (source table, source column)
     */
    def to(table: Table, column: Column) = 
    {
        checkIsSetup(table.databaseName)
        referenceData(table.databaseName).filter { case(_, _, targetTable, targetColumn) => 
                targetTable == table && targetColumn == column }.map { 
                case(sourceTable, sourceColumn, _, _) => (sourceTable, sourceColumn) }
    }
    
    /**
     * Finds all places where the provided column is referenced
     * @param table the table that contains the column
     * @param columnnName the name of the referenced column
     * @return All references to the specified column (source table, source column)
     */
    def to(table: Table, columnName: String): Set[Tuple2[Table, Column]] = 
            table(columnName).map { to(table, _) }.getOrElse(HashSet());
    
    /**
     * Finds all references made from a specific table (source column, target table, target column)
     */
    def from(table: Table) = 
    {
        checkIsSetup(table.databaseName)
        referenceData(table.databaseName).filter { case(sourceTable, _, _, _) => 
                sourceTable == table }.map { case(_, sourceColumn, targetTable, targetColumn) => 
                (sourceColumn, targetTable, targetColumn) }
    }
    
    /**
     * Finds all references made into a specific table (source table, source column, target column) 
     * where the target column resides inside the provided table
     */
    def to(table: Table) = 
    {
        checkIsSetup(table.databaseName)
        referenceData(table.databaseName).filter { case(_, _, targetTable, _) => 
                targetTable == table }.map { case (sourceTable, sourceColumn, _, targetColumn) => 
                (sourceTable, sourceColumn, targetColumn) }
    }
    
    /**
     * Finds all tables referenced from a certain table
     */
    def tablesReferencedFrom(table: Table) = from(table).map { case (_, targetTable, _) => targetTable }
    
    /**
     * Finds all tables that contain references to the specified table
     */
    def tablesReferencing(table: Table) = to(table).map { case (sourceTable, _, _) => sourceTable }
    
    private def checkIsSetup(databaseName: String) = 
    {
        if (!referenceData.contains(databaseName)) 
        {
            throw EnvironmentNotSetupException(
                    s"References for database '$databaseName' haven't been specified")
        }
    }
}