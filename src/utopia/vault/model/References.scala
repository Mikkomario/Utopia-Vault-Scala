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
     * Sets up reference data for a single database. Each pair should contain 4 elements: 
     * 1) referencing table, 2) name of the referencing property, 3) referenced table, 
     * 4) name of the referenced property.
     */
    def setup(firstSet: Tuple4[Table, String, Table, String], 
            more: Tuple4[Table, String, Table, String]*): Unit = 
    {
        // Converts the tuple data into a reference set
        val references = (HashSet(firstSet) ++ more).flatMap { case (table1, name1, table2, name2) => 
        {
            val column1 = table1.find(name1)
            val column2 = table2.find(name2)
            
            if (column1.isEmpty || column2.isEmpty)
            {
                None
            }
            else
            {
                Some((table1, column1.get, table2, column2.get))
            }
        } }
        
        setup(firstSet._1.databaseName, references)
    }
    
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
            table.find(columnName).flatMap { from(table, _) }
    
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
            table.find(columnName).map { to(table, _) }.getOrElse(HashSet());
    
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
     * Finds all references between the two tables. The results contain pairings of left side 
     * columns matched with right side columns. The references may go either way
     */
    def between(left: Table, right: Table) = 
    {
       checkIsSetup(left.databaseName)
       val sameOrderMatches = referenceData(left.databaseName).filter { case(sourceTable, _, targetTable, _) => 
               sourceTable == left && targetTable == right}.map { case (_, leftColumn, _, rightColumn) => 
               (leftColumn, rightColumn) }
       val oppositeOrderMatches = referenceData(left.databaseName).filter { case(sourceTable, _, targetTable, _) => 
               sourceTable == right && targetTable == left }.map { case (_, rightColumn, _, leftColumn) => 
               (leftColumn, rightColumn)}
               
       sameOrderMatches ++ oppositeOrderMatches
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