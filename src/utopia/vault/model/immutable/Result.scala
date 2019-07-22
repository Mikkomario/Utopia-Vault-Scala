package utopia.vault.model.immutable

import utopia.flow.datastructure.immutable.Value

object Result
{
    /**
      * An empty result
      */
    val empty = Result()
}

/**
 * A result is generated based on the data retrieved from a executed database query. 
 * Usually results are used for accessing read database row data, but in case of an insert
 * statement, the generated indices are also available.
 * @author Mikko Hilpinen
 * @since 25.4.2017
  * @param rows The retrieved data rows (on select)
  * @param generatedKeys Primary keys of newly generated rows (on insert)
  * @param updatedRowCount Number of updated rows (on update)
 */
case class Result(rows: Vector[Row] = Vector(), generatedKeys: Vector[Value] = Vector(), updatedRowCount: Int = 0)
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
      * @return The first value in this result. Should only be used when a single column is selected and query is
      *         limited to 1 row
      */
    def firstValue = rows.headOption.map { _.value } getOrElse Value.empty()
    
    /**
     * Whether this result is empty and doesn't contain any rows or generated keys
     */
    def isEmpty = generatedKeys.isEmpty && rows.isEmpty
    
    /**
      * @return Whether this result contains rows or generated keys
      */
    def nonEmpty = !isEmpty
    
    /**
     * The generated keys in integer format
     */
    def generatedIntKeys = generatedKeys.flatMap { _.int }
    
    /**
     * The generated keys in long format
     */
    def generatedLongKeys = generatedKeys.flatMap { _.long }
    
    /**
      * @return All indices within this result. Won't work properly when rows contain indices from multiple tables.
      */
    def indices = rows.map { _.index }
    
    /**
      * @return The index of the first result row
      */
    def firstIndex = rows.headOption.map { _.index } getOrElse Value.empty()
    
    /**
      * @return Whether the query updated any rows
      */
    def updatedRows = updatedRowCount > 0
    
    
    // IMPLEMENTED  ----------------------
    
    override def toString =
    {
        if (generatedKeys.nonEmpty)
            s"Generated keys: [${ generatedKeys.map { _.getString }.mkString(", ") }]"
        else if (updatedRowCount > 0)
            s"Updated $updatedRowCount row(s)"
        else if (rows.nonEmpty)
            s"${rows.size} Row(s): \n${rows.map { "\t" + _ }.mkString("\n")}"
        else
            "Empty result"
    }
    
    
    // OTHER METHODS    ------------------
    
    /**
     * Retrieves row data concerning a certain table
     * @param table The table whose data is returned
     */
    def rowsForTable(table: Table) = rows.map { _(table) }
    
    /**
      * @param table Target table
      * @return Index results for specified table
      */
    def indicesForTable(table: Table) = rows.map { _.indexForTable(table) }
    
    /**
      * @param table Target table
      * @return The first row index for the specified table
      */
    def firstIndexForTable(table: Table) = rows.headOption.map { _.indexForTable(table) } getOrElse Value.empty()
}