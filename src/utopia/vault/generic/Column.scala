package utopia.vault.generic

import utopia.flow.datastructure.immutable.PropertyDeclaration
import utopia.flow.generic.DataType
import utopia.flow.datastructure.immutable.Value

/**
 * Columns represent database columns and can be used as templates for different properties
 * @author Mikko Hilpinen
 * @since 8.3.2017
 */
class Column(propertyName: String, val columnName: String, dataType: DataType, val notNull: Boolean, 
        val isPrimary: Boolean, val usesAutoIncrement: Boolean, defaultValue: Option[Value] = None) 
        extends PropertyDeclaration(propertyName, dataType, defaultValue)
{
    
}