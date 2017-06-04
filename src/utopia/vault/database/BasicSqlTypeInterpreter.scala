package utopia.vault.database

/**
 * This interpreter is able to interpret the basic sql type cases into the basic data types 
 * introduced in the Flow project
 * @author Mikko Hilpinen
 * @since 4.6.2017
 */
object BasicSqlTypeInterpreter extends SqlTypeInterpreter
{
    def apply(typeString: String) = 
    {
        
        
        typeString.toLowerCase match 
        {
            
            case _ => None
        }
    }
}