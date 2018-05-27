package utopia.vault.sql

import utopia.vault.model.Table
import utopia.flow.datastructure.template.Model
import utopia.flow.datastructure.template.Property
import scala.collection.immutable.HashSet
import utopia.flow.datastructure.immutable.Value
import utopia.flow.datastructure.immutable
import scala.collection.immutable.HashMap

/**
 * This object is used for generating update statements that modify database row contents
 * @author Mikko Hilpinen
 * @since 25.5.2017
 */
object Update
{
    // OPERATORS    ----------------------
    
    /**
     * Creates an sql segment that updates one or multiple tables
     * @param target The target portion for the update segment. This may consist of a single or 
     * multiple tables, but must contain all tables that are introduced in the 'set'
     * @param set New value assignments for each of the modified tables. Property names are used 
     * as model keys, they will be converted to column names automatically
     * @return an update segment or none if there is nothing to update
     */
    def apply(target: SqlTarget, set: Map[Table, Model[Property]]) = 
    {
        val valueSet = set.flatMap { case (table, model) => model.attributes.flatMap { 
                property => table.find(property.name).map { (_, property.value) } } }
        
        if (valueSet.isEmpty)
            None
        else 
            Some(target.toSqlSegment.prepend("UPDATE") + SqlSegment("SET " + 
                    valueSet.view.map { case (column, value) => 
                    column.columnNameWithTable + " = ?" }.reduceLeft { _ + ", " + _ }, 
                    valueSet.map { _._2 }.toVector))
    }
    
    /**
     * Creates an update segment that changes multiple values in a table
     * @return an update segment or none if there is nothing to update
     */
    def apply(table: Table, set: Model[Property]): Option[SqlSegment] = apply(table, HashMap(table -> set))
    
    /**
     * Creates an update segment that changes the value of a single column in the table
     * @return an update segment or none if there is nothing to update
     */
    def apply(table: Table, key: String, value: Value): Option[SqlSegment] = apply(table, 
            immutable.Model(Vector((key, value))));
}