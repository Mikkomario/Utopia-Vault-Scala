package utopia.vault.database

/**
 * Conditions can be combined with each other logically and converted to sql where clauses. 
 * A where clause is often used after a join or a basic operation.
 * @author Mikko Hilpinen
 * @since 22.5.2017
 */
case class Condition(private val segment: SqlSegment)
{
    // COMPUTED PROPERTIES    ---------------
    
    override def toString = toWhereClause.toString()
    
    /**
     * Converts this condition into a real sql segment that can function as a where clause
     */
    def toWhereClause = segment prepend "WHERE"
    
    
    // OPERATORS    -------------------------
    
    //def &&(other: Condition) = Condition(this.segment + "AND" + other.segment)
    
    //def ||(other: Condition) = Condition(this.segment + "OR" + other.segment)
}