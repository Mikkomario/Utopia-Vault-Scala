package utopia.vault.model

object ReferencePoint
{
    /**
     * Finds a reference point from a table
     * @param the table for the reference point
     * @param the property name for the associated column
     */
    def apply(table: Table, propertyName: String) = table.find(propertyName).map(
            new ReferencePoint(table, _))
}

/**
* A reference point is simply information about a certain column in a table that contains or 
* is targeted by a reference
* @author Mikko Hilpinen
* @since 21.5.2018
**/
case class ReferencePoint(val table: Table, val column: Column)