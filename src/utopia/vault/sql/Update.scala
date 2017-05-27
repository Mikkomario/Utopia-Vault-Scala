package utopia.vault.sql

import utopia.vault.model.Table
import utopia.flow.datastructure.template.Model
import utopia.flow.datastructure.template.Property
import scala.collection.immutable.HashSet
import utopia.flow.datastructure.immutable.Value
import utopia.flow.datastructure.immutable

/**
 * This object is used for generating update statements that modify database row contents
 * @author Mikko Hilpinen
 * @since 25.5.2017
 */
object Update
{
    // OPERATORS    ----------------------
    
    /**
     * Creates an update segment that changes multiple values in a table
     */
    def apply(table: Table, set: Model[Property]) = 
    {
        val valueSet = set.attributeMap.flatMap { case (name, property) => table(name).map { (_, property.value) } }
        
        if (valueSet.isEmpty)
        {
            SqlSegment.empty
        }
        else 
        {
            SqlSegment(s"UPDATE ${ table.name } SET ${ valueSet.view.map { case (column, value) => 
                    s"${ column.columnNameWithTable } = ?" }.reduceLeft { _ + ", " + _ } }", 
                    valueSet.map { case (_, value) => value }.toVector, Some(table.databaseName), 
                    HashSet(table))
        }
    }
    
    /**
     * Creates an update segment that changes the value of a single column in the table
     */
    def apply(table: Table, key: String, value: Value): SqlSegment = apply(table, 
            immutable.Model(Vector((key, value))));
}