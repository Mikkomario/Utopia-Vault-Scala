package utopia.vault.sql

import utopia.flow.datastructure.immutable.Value

/**
 * This object is used for creating sql segments that limit the amount of resulting rows. 
 * The limit is usually placed at the end of the sql statement
 * @author Mikko Hilpinen
 * @since 27.5.2017
 */
object Limit
{
    /**
     * Creates a new sql segment that limits the amount of returned rows to that of the parameter value
     * @param maxRows How many rows should the result contain at maximum
     */
    def apply(maxRows: Int) = SqlSegment("LIMIT ?", Vector(Value of maxRows))
}