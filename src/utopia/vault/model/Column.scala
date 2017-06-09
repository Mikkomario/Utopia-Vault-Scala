package utopia.vault.model

import utopia.flow.datastructure.immutable.PropertyDeclaration
import utopia.flow.generic.DataType
import utopia.flow.datastructure.immutable.Value
import utopia.vault.sql.SqlSegment
import utopia.vault.sql.ConditionElement
import utopia.vault.sql.Condition

// TODO: Possibly remove the non-null feature
/**
 * Columns represent database columns and can be used as templates for different properties
 * @author Mikko Hilpinen
 * @since 8.3.2017
 */
class Column(propertyName: String, val columnName: String, val tableName: String, 
        dataType: DataType, defaultValue: Option[Value] = None, 
        val isPrimary: Boolean = false, val usesAutoIncrement: Boolean = false) 
        extends PropertyDeclaration(propertyName, dataType, defaultValue) with ConditionElement
{
    // COMPUTED PROPERTIES    ------------------
    
    override def properties = super.properties ++ Vector(columnName, isPrimary, usesAutoIncrement)
    
    override def toString = s"$columnName $dataType ${ if (isPrimary) "PRIMARY KEY " else ""} ${
            if (usesAutoIncrement) "AUTO_INCREMENT " else ""}"
    
    override def toSqlSegment = SqlSegment(columnNameWithTable)
            
    /**
     * Whether a value is required in this column when data is inserted to the database
     */
    // def isRequiredInInsert = notNull && !defaultValue.exists { _.isDefined } && !usesAutoIncrement
    
    /**
     * The name of the column, including the table name for disambiguity
     */
    def columnNameWithTable = tableName + "." + columnName
    
    /**
     * Creates a condition that checks whether the column value in the database is null
     */
    def isNull = Condition(SqlSegment(columnNameWithTable + " IS NULL"))
    
    /**
     * Creates a condition that checks whether the column value in the database is not null
     */
    def isNotNull = Condition(SqlSegment(columnNameWithTable + " IS NOT NULL"))
    
    
    // OPERATORS    ----------------------------
    
    /**
     * Creates an equality condition between a column and a specified value. This condition can 
     * be then used in a sql statement. Calling this with an empty value is same as calling isNull
     */
    def <=>(value: Value) = if (value.isEmpty) isNull else makeCondition("<=>", value)
    
    /**
     * Creates a not equals condition between a column and a specified value. This condition can 
     * be used in a sql statement. Calling this with an empty value is same as calling isNotNull
     */
    def <>(value: Value) = if (value.isEmpty) isNotNull else makeCondition("<>", value)
       
    
    // OTHER METHODS    ---------------------
    
    private def makeCondition(operator: String, value: Value) = Condition(
            SqlSegment(s"$columnNameWithTable $operator ?", Vector(value)));
}