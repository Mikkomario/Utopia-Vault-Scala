package utopia.vault.sql

import utopia.vault.model.immutable.Storable

object Where
{
    /**
     * This is an alternative way of converting a condition into a where clause. Does the 
     * same as condition.toWhereClause
     */
    def apply(condition: Condition) = condition.toWhereClause
    
    /**
      * A utility method for converting a storable instance into a where clause. Same as calling
      * storable.toCondition.toWhereClause
      * @param conditionModel A storable model representing a condition
      * @return A where clause based on the model
      */
    def apply(conditionModel: Storable) = conditionModel.toCondition.toWhereClause
}

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
    def &&(first: Condition, second: Condition, more: Condition*): Condition = this && (Vector(first, second) ++ more)
    
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
    def ||(first: Condition, second: Condition, more: Condition*): Condition = this || (Vector(first, second) ++ more)
    
    /**
     * Applies a logical NOT operator on this condition, reversing any logical outcome
     */
    def unary_! = Condition(segment.copy(sql = s"NOT (${ segment.sql })"))
    
    
    // OTHER METHODS    ---------------------
    
    /**
     * Combines this and another condition together using a logical XOR. The logical value is true 
     * when both of the conditions have different values
     */
    def xor(other: Condition) = combine(Vector(other), " XOR ")
    
    private def combine(others: Seq[Condition], separator: String) = 
    {
        if (others.isEmpty)
            this
        else 
        {
            val noParentheses = SqlSegment.combine(segment +: others.map { _.segment }, { _ + separator + _ })
            Condition(noParentheses.copy(sql = "(" + noParentheses.sql + ")"))
        }
    }
}