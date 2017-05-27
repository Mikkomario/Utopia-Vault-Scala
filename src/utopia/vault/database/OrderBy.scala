package utopia.vault.database

import utopia.vault.generic.Column

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
     * @param column The columns used in the ordering (most important comes first). Each column 
     * is tied to a boolean value describing whether the order should be ascending (true) or 
     * descending (false)
     */
    def apply(columns: Seq[Tuple2[Column, Boolean]]) = 
    {
        if (columns.isEmpty)
        {
            SqlSegment.empty
        }
        else 
        {
            val sqlParts = columns.map{ case (column, ascending) => 
                    column.columnNameWithTable + " " + (if (ascending) "ASC" else "DESC") }
            SqlSegment("ORDER BY " + sqlParts.reduceLeft { _ + ", " + _ })
        }
    }
    
    /**
     * Creates a new sql segment that orders by a single column either ascending or descending
     * @param the column by which the results are ordered
     * @param ascending Whether the results should be ascending (true, default) or descending (false)
     */
    def apply(column: Column, ascending: Boolean = true): SqlSegment = apply(Vector((column, ascending)))
    
    /**
     * Creates a new sql segment that orders by multiple columns using a either ascending or 
     * descending order for each
     */
    def apply(ascending: Boolean, first: Column, second: Column, more: Column*): SqlSegment = 
            apply((Vector(first, second) ++ more).map { (_, ascending) });
}