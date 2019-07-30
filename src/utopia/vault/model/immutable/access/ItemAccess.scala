package utopia.vault.model.immutable.access

import utopia.flow.datastructure.immutable.Value
import utopia.vault.database.Connection
import utopia.vault.model.immutable.factory.FromResultFactory
import utopia.vault.sql.{Delete, Where}

/**
 * Provides access to a single possible id / row in a database
 * @author Mikko Hilpinen
 * @since 30.7.2019, v1.3+
 * @tparam A Type of model read from table
 * @param value Value of this id
 * @param factory Factory used for reading model data from table
 */
class ItemAccess[+A](value: Value, factory: FromResultFactory[A])
{
	/**
	 * @return A condition based on this id
	 */
	def toCondition = factory.table.primaryColumn.get <=> value
	
	/**
	 * Checks whether this index is valid (exists in database)
	 * @param connection Database connection (implicit)
	 * @return Whether this id exists in the database
	 */
	def exists(implicit connection: Connection) = factory.table.containsIndex(value)
	
	/**
	 * Reads model data from table
	 * @param connection Database connection (implicit)
	 * @return Model read for this id
	 */
	def get(implicit connection: Connection) = factory.getMany(toCondition).headOption
	
	/**
	 * Deletes this id from database
	 * @param connection Database connection (implicit)
	 * @return Whether any rows were deleted
	 */
	def delete(implicit connection: Connection) = connection(Delete(factory.table) + Where(toCondition)).updatedRows
}
