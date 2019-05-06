package utopia.vault.model

import utopia.flow.datastructure.immutable.Model
import utopia.flow.datastructure.immutable.Constant

/**
 * A row represents a single row in a query result set. A row can contain columns from multiple
 * tables, if it is generated from a join query
 * @author Mikko Hilpinen
 * @since 30.4.2017
 * @param columnData Column data contains models generated on retrieved columns. There's a separate
 * model for each table. The table's name is used as a key in this map.
 */
case class Row(columnData: Map[Table, Model[Constant]])
{
    // ATTRIBUTES   ---------------------------
    
    /**
      * This row represented as a single model. If there are results from multiple tables in this
      * row, this model may not contain all of that data because of duplicate attribute names.
      */
    lazy val toModel = if (columnData.nonEmpty) columnData.values.reduce { _ ++ _ } else Model.empty
    
    
    // COMPUTED PROPERTIES    -----------------
    
    /**
     * Whether this row is empty and contains no column value data at all
     */
    def isEmpty = columnData.values.forall { _.isEmpty }
    
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
    def apply(table: Table) = columnData.getOrElse(table, Model.empty)
    
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