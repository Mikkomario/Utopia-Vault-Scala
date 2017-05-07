package utopia.vault.database

import utopia.vault.generic.Table
import utopia.flow.datastructure.immutable.Model
import utopia.flow.datastructure.immutable.Constant

/**
 * A result is generated based on the data retrieved from a executed database query. 
 * Usually results are used for accessing read database row data, but in case of an insert
 * statement, the generated indices are also available.
 * @author Mikko Hilpinen
 * @since 25.4.2017
 */
class Result(val rows: Vector[Row], val generatedKeys: Vector[Int])
{
    // COMPUTED PROPERTIES    ------------
    
    /**
     * The row data in model format. This should be used when no joins were present in the query and
     * each row contains data from a single table only
     */
    def rowModels = rows.map { _.toModel }
    
    /**
     * Whether this result is empty and doesn't contain any meaningful data
     */
    def isEmpty = generatedKeys.isEmpty && rows.forall { _.isEmpty }
    
    
    // OTHER METHODS    ------------------
    
    /**
     * Retrieves row data concerning a certain table
     * @param table The table whose data is returned
     */
    def rowsForTable(table: Table) = rows.map { _(table) }
}