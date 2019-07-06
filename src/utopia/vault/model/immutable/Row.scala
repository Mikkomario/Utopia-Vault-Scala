package utopia.vault.model.immutable

import utopia.flow.datastructure.immutable.{Constant, Model, Value}

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
    
    /**
      * @return An index from this row. If this row contains data from multiple tables, please use index(Table) or
      *         indices instead.
      */
    def index = indices.headOption.map { _._2 } getOrElse Value.empty()
    
    
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
    
    /**
      * @param column Target column
      * @return The value for the specified column
      */
    def apply(column: Column) = columnData.find { _._1.contains(column) }.map { _._2(column.name) }
        .getOrElse(Value.empty(column.dataType))
    
    
    // OTHER METHODS    ----------------------
    
    /**
     * Finds the index of the row in the specified table
     */
    def indexForTable(table: Table) = table.primaryColumn.map { column => apply(table)(column.name) }
        .getOrElse(Value.empty())
}