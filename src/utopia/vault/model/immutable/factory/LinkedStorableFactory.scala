package utopia.vault.model.immutable.factory
import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.vault.model.immutable.Row
import utopia.vault.util.ErrorHandling

import scala.util.{Failure, Success, Try}

/**
 * Used for converting DB data to models for tables that contain a single link to another table / model
 * @author Mikko
 * @since 21.8.2019, v1.3.1+
 */
trait LinkedStorableFactory[+Parent, Child] extends FromRowFactory[Parent]
{
	// ABSTRACT	---------------------
	
	/**
	 * @return Factory used for parsing linked child data
	 */
	def childFactory: FromRowFactory[Child]
	
	/**
	 * Parses model & child data into a complete parent model. Parsing failures are handled by
	 * ErrorHandling.modelParsePrinciple
	 * @param model Parent model data (not validated)
	 * @param child Parsed child
	 * @return Parsed parent data. May fail.
	 */
	def apply(model: Model[Constant], child: Child): Try[Parent]
	
	
	// IMPLEMENTED	-----------------
	
	override def apply(row: Row) = childFactory(row).flatMap { c => apply(row(table), c) }
	
	override def joinedTables = childFactory.tables
}
