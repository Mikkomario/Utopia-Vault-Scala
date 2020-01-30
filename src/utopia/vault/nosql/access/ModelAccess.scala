package utopia.vault.nosql.access

import utopia.vault.model.immutable.factory.FromResultFactory

/**
 * Common trait for access points that return parsed model data
 * @author Mikko Hilpinen
 * @since 30.1.2020, v1.4
 */
trait ModelAccess[+A] extends Access[A]
{
	// ABSTRACT	-------------------------
	
	/**
	 * @return The factory used for parsing accessed data
	 */
	def factory: FromResultFactory[A]
	
	
	// IMPLEMENTED	---------------------
	
	final override def table = factory.table
}
