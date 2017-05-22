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
    
    /**
     * Combines the conditions together using a logical AND. All of the conditions are wrapped in 
     * single parentheses '()' and performed together, from left to right.
     */
    def &&(others: Seq[Condition]) = combine(others, " AND ")
    
    /**
     * Combines this and another condition together using a logical AND. The conditions are wrapped in 
     * single parentheses '()' and performed together, from left to right.
     */
    def &&(other: Condition): Condition = this && Vector(other)
    
    /**
     * Combines the conditions together using a logical AND. All of the conditions are wrapped in 
     * single parentheses '()' and performed together, from left to right.
     */
    def &&(first: Condition, second: Condition, more: Condition*): Condition = 
            this && (Vector(first, second) ++ more);
    
    /**
     * Combines the conditions together using a logical OR. All of the conditions are wrapped in 
     * single parentheses '()' and performed together, from left to right.
     */
    def ||(others: Seq[Condition]) = combine(others, " OR ")
    
    /**
     * Combines this and another condition together using a logical OR. The conditions are wrapped in 
     * single parentheses '()' and performed together, from left to right.
     */
    def ||(other: Condition): Condition = this || Vector(other)
    
    /**
     * Combines the conditions together using a logical OR. All of the conditions are wrapped in 
     * single parentheses '()' and performed together, from left to right.
     */
    def ||(first: Condition, second: Condition, more: Condition*): Condition = 
            this || (Vector(first, second) ++ more);
    
    
    // OTHER METHODS    ---------------------
    
    /**
     * Combines this and another condition together using a logical XOR. The logical value is true 
     * when both of the conditions have different values
     */
    def xor(other: Condition) = combine(Vector(other), " XOR ")
    
    private def combine(others: Seq[Condition], separator: String) = 
    {
        if (others.isEmpty)
        {
            this
        }
        else 
        {
            val otherSegments = others.map { _.segment }
            val allSegments = segment +: otherSegments
            
            val sql = "(" + (otherSegments.foldLeft(segment.sql){ _ + separator + _.sql }) + ")"
            val databaseName = allSegments.flatMap { _.databaseName }.headOption
            
            Condition(SqlSegment(sql, allSegments.flatMap { _.values }, databaseName, 
                    allSegments.flatMap { _.selectedTables }.toSet, 
                    allSegments.exists { _.generatesKeys }))
        }
    }
}