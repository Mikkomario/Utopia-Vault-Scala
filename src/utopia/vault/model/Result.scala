package utopia.vault.model

import utopia.flow.util.Equatable
import utopia.flow.datastructure.immutable.Value

/**
 * A result is generated based on the data retrieved from a executed database query. 
 * Usually results are used for accessing read database row data, but in case of an insert
 * statement, the generated indices are also available.
 * @author Mikko Hilpinen
 * @since 25.4.2017
 */
class Result(val rows: Vector[Row], val generatedKeys: Vector[Value] = Vector()) extends Equatable
{
    // COMPUTED PROPERTIES    ------------
    
    override def properties = rows ++ generatedKeys
    
    /**
     * The row data in model format. This should be used when no joins were present in the query and
     * each row contains data from a single table only
     */
    def rowModels = rows.map { _.toModel }
    
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