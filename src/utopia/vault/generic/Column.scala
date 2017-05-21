package utopia.vault.generic

import utopia.flow.datastructure.immutable.PropertyDeclaration
import utopia.flow.generic.DataType
import utopia.flow.datastructure.immutable.Value

/**
 * Columns represent database columns and can be used as templates for different properties
 * @author Mikko Hilpinen
 * @since 8.3.2017
 */
class Column(propertyName: String, val columnName: String, dataType: DataType, val notNull: Boolean = false, 
        val isPrimary: Boolean = false, val usesAutoIncrement: Boolean = false, defaultValue: Option[Value] = None) 
        extends PropertyDeclaration(propertyName, dataType, defaultValue)
{
    // COMPUTED PROPERTIES    ------------------
    
    override def properties = super.properties ++ Vector(columnName, notNull, isPrimary, usesAutoIncrement)
    
    override def toString = s"$columnName $dataType ${if (notNull) "NOT NULL " else ""} ${
            if (isPrimary) "PRIMARY KEY " else ""} ${if (usesAutoIncrement) "AUTO_INCREMENT " else ""}";
            
    /**
     * Whether a value is required in this column when data is inserted to the database
     */
    def isRequiredInInsert = notNull && !defaultValue.exists { _.isDefined } && !usesAutoIncrement
}