package utopia.vault.sql

/**
 * ConditionElements are elements used in logical conditions. Usually two or more elements are 
 * compared in some way
 * @author Mikko Hilpinen
 * @since 25.5.2017
 */
trait ConditionElement
{
    // ABSTRACT METHODS    ----------------------
    
    /**
     * Converts this condition element into an sql segment
     */
    def toSqlSegment: SqlSegment
    
    
    // OPERATORS    -----------------------------
    
    /**
     * Creates an equality condition between two elements
     */
    def <=>(other: ConditionElement) = makeCondition("<=>", other)
    
    /**
     * Creates a not equals condition between two elements
     */
    def <>(other: ConditionElement) = makeCondition("<>", other)
    
    /**
     * Creates a larger than condition
     */
    def >(other: ConditionElement) = makeCondition(">", other)
    
    /**
     * Creates a larger than or equals condition
     */
    def >=(other: ConditionElement) = makeCondition(">=", other)
    
    /**
     * Creates a smaller than condition
     */
    def <(other: ConditionElement) = makeCondition("<", other)
    
    /**
     * Creates a smaller than or equals condition
     */
    def <=(other: ConditionElement) = makeCondition("<=", other)
    
    
    // OTHER METHODS    ---------------------
    
    /**
     * Creates a between condition that returns true when this element value is between the two 
     * provided values (inclusive)
     */
    def isBetween(min: ConditionElement, max: ConditionElement) = Condition(toSqlSegment + "BETWEEN" + 
            min.toSqlSegment + "AND" + max.toSqlSegment);
    
    /**
     * Creates an in condition that returns true if one of the provided element values matches 
     * this element's value
     */
    def in(elements: Seq[ConditionElement]) = 
    {
        val rangeSegment = SqlSegment.combine(elements.map { _.toSqlSegment }, { _ + ", " + _ })
        val inSegment = rangeSegment.copy(sql = "(" + rangeSegment.sql + ")")
        
        Condition(toSqlSegment + "IN" + inSegment)
    }
    
    private def makeCondition(operator: String, other: ConditionElement) = Condition(
            toSqlSegment + operator + other.toSqlSegment);
}