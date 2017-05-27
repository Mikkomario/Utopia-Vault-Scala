package utopia.vault.sql

import utopia.vault.model.Table
import scala.collection.immutable.HashSet

/**
 * This object generates sql segments used for selecting / retrieving all data from the targeted 
 * database rows.
 * @author Mikko Hilpinen
 * @since 22.5.2017
 */
object SelectAll
{
    /**
     * Creates an sql segment that is used for retrieving data from all the columns from the 
     * targeted rows. This statement can then be followed by a join- or where clause and possibly 
     * a limit
     */
    def apply(table: Table) = SqlSegment(s"SELECT * FROM ${ table.name }", Vector(), 
            Some(table.databaseName), HashSet(table), true);
}