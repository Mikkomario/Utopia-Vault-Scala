package utopia.vault.model

import utopia.flow.datastructure.immutable.Value

object Result
{
    /**
      * An empty result
      */
    val empty = Result(Vector())
}

/**
 * A result is generated based on the data retrieved from a executed database query. 
 * Usually results are used for accessing read database row data, but in case of an insert
 * statement, the generated indices are also available.
 * @author Mikko Hilpinen
 * @since 25.4.2017
 */
case class Result(rows: Vector[Row], generatedKeys: Vector[Value] = Vector())
{
    // COMPUTED PROPERTIES    ------------
    
    /**
     * The row data in model format. This should be used when no joins were present in the query and
     * each row contains data from a single table only
     */
    def rowModels = rows.map { _.toModel }
    
    /**
     * The data of the first result row in model format. This should be used only when no joins 
     * were present in the query and each row contains data from a single table only
     */
    def firstModel = rows.headOption.map { _.toModel }
    
    /**
     * Whether this result is empty and doesn't contain any meaningful data
     */
    def isEmpty = generatedKeys.isEmpty && rows.forall { _.isEmpty }
    
    /**
     * The generated keys in integer format
     */
    def generatedIntKeys = generatedKeys.flatMap { _.int }
    
    /**
     * The generated keys in long format
     */
    def generatedLongKeys = generatedKeys.flatMap { _.long }
    
    
    // OTHER METHODS    ------------------
    
    /**
     * Retrieves row data concerning a certain table
     * @param table The table whose data is returned
     */
    def rowsForTable(table: Table) = rows.map { _(table) }
}