package utopia.vault.database

import utopia.flow.datastructure.immutable.Model
import utopia.flow.datastructure.immutable.Constant
import utopia.vault.generic.Table

/**
 * A row represents a single row in a query result set. A row can contain columns from multiple
 * tables, if it is generated from a join query
 * @author Mikko Hilpinen
 * @since 30.4.2017
 * @param columnData Column data contains models generated on retrieved columns. There's a separate
 * model for each table. The table's name is used as a key in this map.
 */
class Row(val columnData: Map[String, Model[Constant]])
{
    // COMPUTED PROPERTIES    -----------------
    
    /**
     * Whether this row is empty and contains no column value data at all
     */
    def isEmpty = columnData.values.forall { _.isEmpty }
    
    /**
     * This row represented as a single model. If there are results from multiple tables in this
     * row, this model may not contain all of that data because of duplicate attribute names.
     */
    def toModel = if (!columnData.isEmpty) columnData.values.reduce { _ ++ _ } else new Model(Vector())
    
    
    // OPERATORS    ---------------------------
    
    /**
     * Finds column data for a table with the provided name
     * @param tableName The name of the table whose data is requested
     */
    def apply(tableName: String) = columnData.getOrElse(tableName, new Model(Vector()))
    
    /**
     * Finds column data for a table
     * @param table The table whose data is requested
     */
    def apply(table: Table): Model[Constant] = apply(table.name)
}