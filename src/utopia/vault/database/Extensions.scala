package utopia.vault.database

import utopia.flow.datastructure.immutable.Value

import scala.language.implicitConversions

/**
 * This object contains some extensions provided by the vault project
 * @author Mikko Hilpinen
 * @since 25.5.2017
 */
object Extensions
{
    /**
     * This extension allows values to be used as condition elements
     */
    implicit class ConditionValue(val value: Value) extends ConditionElement
    {
        // COMPUTED PROPERTIES    ------------------
        
        def toSqlSegment = SqlSegment("?", Vector(value))
    }
}