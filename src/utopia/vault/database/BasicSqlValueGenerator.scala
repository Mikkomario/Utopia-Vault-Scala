package utopia.vault.database

import java.sql.Types
import utopia.flow.datastructure.immutable.Value
import utopia.flow.generic.LongType
import utopia.flow.generic.DataType
import utopia.flow.generic.BooleanType
import utopia.flow.generic.StringType
import utopia.flow.generic.FloatType
import utopia.flow.generic.IntType
import java.sql.Date
import java.sql.Timestamp
import utopia.flow.generic.ValueConversions._

/**
 * This generator handles value generation for the basic data types introduced in the Flow project. 
 * @author Mikko Hilpinen
 * @since 28.4.2017
 */
object BasicSqlValueGenerator extends SqlValueGenerator
{
    // IMPLEMENTED METHODS    ---------------
    
    override def apply(value: Any, sqlType: Int) = 
    {
        sqlType match 
        {
            case Types.TIMESTAMP | Types.TIMESTAMP_WITH_TIMEZONE => 
                Some(value.asInstanceOf[Timestamp].toInstant())
            case Types.CHAR => Some(value.toString)
            
            case Types.INTEGER | Types.SMALLINT | Types.TINYINT => wrap(value, IntType)
            case Types.FLOAT => wrap(value, FloatType)
            case Types.VARCHAR => wrap(value, StringType)
            case Types.BOOLEAN => wrap(value, BooleanType)
            case Types.BIGINT => wrap(value, LongType)
            
            case _ => None
        }
    }
    
    
    // OTHER METHODS    ---------------------
    
    private def wrap(value: Any, toType: DataType) = Some(new Value(Some(value), toType))
}