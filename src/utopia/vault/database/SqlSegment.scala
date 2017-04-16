package utopia.vault.database

import utopia.flow.datastructure.immutable.Value

/**
 * Sql Segments can be combined to form sql statements. Some may contain value assignments too.
 * @author Mikko Hilpinen
 * @since 12.3.2017
 */
trait SqlSegment
{
    // ABSTRACT METHODS    ------------------
    
    /**
     * The sql string representing the segment. Each of the segment's values is indicated with a 
     * '?' character.
     */
    def sql: String
    
    /**
     * The values that will be inserted to this segment when it is used. Each '?' in the sql will 
     * be replaced with a single value. Empty values will be interpreted as NULL.<br>
     * The default implementation doesn't contain any values
     */
    def values: Vector[Value]
    
    
    // OTHER METHODS    ---------------------
    
    
}