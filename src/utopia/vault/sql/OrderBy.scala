package utopia.vault.sql

import utopia.vault.model.immutable.Column

/**
 * This object is used for generating sql segments that determine how the results will be ordered
 * @author Mikko Hilpinen
 * @since 27.5.2017
 */
object OrderBy
{
    /**
     * Creates a new sql segment that orders by multiple columns where the order may be ascending 
     * or descending based on the column
     * @param columns The columns used in the ordering (most important comes first). Each column
     * is tied to a boolean value describing whether the order should be ascending (true) or 
     * descending (false)
     */
    def apply(columns: Seq[(Column, Boolean)]) =
    {
        if (columns.isEmpty)
            SqlSegment.empty
        else 
        {
            val sqlParts = columns.map{ case (column, ascending) => 
                    column.columnNameWithTable + " " + (if (ascending) "ASC" else "DESC") }
            SqlSegment("ORDER BY " + sqlParts.mkString(", "))
        }
    }
    
    /**
     * Creates a new sql segment that orders by a single column either ascending or descending
     * @param column the column by which the results are ordered
     * @param ascending Whether the results should be ascending (true, default) or descending (false)
     */
    def apply(column: Column, ascending: Boolean = true): SqlSegment = apply(Vector((column, ascending)))
    
    /**
     * Creates a new sql segment that orders by multiple columns using a either ascending or 
     * descending order for each
     */
    def apply(ascending: Boolean, first: Column, second: Column, more: Column*): SqlSegment = 
            apply((Vector(first, second) ++ more).map { (_, ascending) })
    
    /**
      * Orders by specified column(s), ascending (= smallest to largest)
      * @param first The first order column
      * @param more More order columns
      * @return An order by segment
      */
    def ascending(first: Column, more: Column*) = apply((first +: more).map { _ -> true })
    
    /**
      * Orders by specified column(s), descending (= largest to smallest)
      * @param first The first order column
      * @param more More order columns
      * @return An order by segment
      */
    def descending(first: Column, more: Column*) = apply((first +: more).map { _ -> false })
}