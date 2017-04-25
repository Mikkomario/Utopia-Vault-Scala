package utopia.vault.database

import utopia.vault.generic.Table
import utopia.flow.datastructure.immutable.Model
import utopia.flow.datastructure.immutable.Constant

/**
 * A result is generated based on the data retrieved from a executed database query results. 
 * Usually results are used for accessing read database row data, but in case of an insert
 * statement, the generated indices are also available
 * @author Mikko Hilpinen
 * @since 25.4.2017
 */
class Result(val data: Map[Table, Vector[Model[Constant]]], val generatedKeys: Vector[Int])
{
    // COMPUTED PROPERTIES    ------------
    
    /**
     * All database row data for all queried tables. The tables are in no specific order. This can
     * be used as a convenience accessor when only one table has been queried
     */
    def rows = data.values.flatten
}