package utopia.vault.model

import utopia.flow.datastructure.immutable.Model
import utopia.flow.datastructure.immutable.Constant
import utopia.flow.util.Equatable

/**
 * A row represents a single row in a query result set. A row can contain columns from multiple
 * tables, if it is generated from a join query
 * @author Mikko Hilpinen
 * @since 30.4.2017
 * @param columnData Column data contains models generated on retrieved columns. There's a separate
 * model for each table. The table's name is used as a key in this map.
 */
class Row(val columnData: Map[Table, Model[Constant]]) extends Equatable
{
    // COMPUTED PROPERTIES    -----------------
    
    override def properties = columnData
    
    /**
     * Whether this row is empty and contains no column value data at all
     */
    def isEmpty = columnData.values.forall { _.isEmpty }
    
    /**
     * This row represented as a single model. If there are results from multiple tables in this
     * row, this model may not contain all of that data because of duplicate attribute names.
     */
    def toModel = if (!columnData.isEmpty) columnData.values.reduce { _ ++ _ } else new Model(Vector())
    
    /**
     * The indices for each of the contained table
     */
    def indices = columnData.flatMap { case (table, model) => table.primaryColumn.flatMap { column => 
            model.findExisting(column.name).map { constant => (table, constant.value) } } }
    
    
    // OPERATORS    ---------------------------
    
    /**
     * Finds column data for a table
     * @param table The table whose data is requested
     */
    def apply(table: Table) = columnData.getOrElse(table, new Model(Vector()))
    
    /**
     * Finds the value of a single property in the row
     * @param propertyName the name of the property
     */
    def apply(propertyName: String) = toModel(propertyName)
    
    
    // OTHER METHODS    ----------------------
    
    /**
     * Finds the index of the row in the specified table
     */
    def index(table: Table) = table.primaryColumn.map { column => apply(table)(column.name) }
}