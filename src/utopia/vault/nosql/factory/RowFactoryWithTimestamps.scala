package utopia.vault.nosql.factory
import java.time.Instant

import utopia.flow.generic.ValueConversions._
import utopia.vault.sql.Extensions._
import utopia.vault.database.Connection
import utopia.vault.model.enumeration.ComparisonOperator
import utopia.vault.model.enumeration.ComparisonOperator.{Larger, LargerOrEqual, Smaller, SmallerOrEqual}
import utopia.vault.sql.{Condition, OrderBy}

/**
  * A common trait for factories that track row creation time
  * @author Mikko Hilpinen
  * @since 1.2.2020, v1.4
  */
trait RowFactoryWithTimestamps[+A] extends FromRowFactory[A]
{
	// ABSTRACT	------------------------
	
	/**
	  * @return Name of the property that represents item creation time
	  */
	def creationTimePropertyName: String
	
	
	// COMPUTED	-------------------------
	
	/**
	  * @return Column that specifies row creation time
	  */
	def creationTimeColumn = table(creationTimePropertyName)
	
	/**
	  * @return Default ordering used by this factory (by default returns first the latest items)
	  */
	def defaultOrdering = OrderBy.descending(creationTimeColumn)
	
	/**
	  * @return The latest recorded item
	  */
	def latest(implicit connection: Connection) = getMax(creationTimeColumn)
	
	/**
	  * @return The earliest recorded item
	  */
	def earliest(implicit connection: Connection) = getMin(creationTimeColumn)
	
	
	// IMPLEMENTED	---------------------
	
	override def get(where: Condition, order: Option[OrderBy])(implicit connection: Connection) =
	{
		// Uses default ordering if no other ordering has been specified
		super.get(where, Some(order.getOrElse(defaultOrdering)))
	}
	
	
	// OTHER	-------------------------
	
	/**
	  * Takes latest n items from the database
	  * @param maxNumberOfItems Maximum number of items to return
	  * @param connection DB Connection (implicit)
	  * @return Latest 'maxNumberOfItems' items from database
	  */
	def takeLatest(maxNumberOfItems: Int)(implicit connection: Connection) = take(maxNumberOfItems, defaultOrdering)
	
	/**
	  * @param threshold Time threshold
	  * @param operator An operator used for comparing row creation times with specified threshold
	  * @return A condition that accepts rows based on specified threshold and operator
	  */
	def creationCondition(threshold: Instant, operator: ComparisonOperator) = creationTimeColumn.makeCondition(operator, threshold)
	
	/**
	  * @param threshold Time threshold
	  * @param isInclusive Whether the threshold should be included in return values (default = false)
	  * @return A condition that accepts rows that were created before the specified time threshold
	  */
	def createdBeforeCondition(threshold: Instant, isInclusive: Boolean = false) = creationCondition(threshold,
		if (isInclusive) SmallerOrEqual else Smaller)
	
	/**
	  * @param threshold Time threshold
	  * @param isInclusive Whether the threshold should be included in return values (default = false)
	  * @return A condition that accepts rows that were created after the specified time threshold
	  */
	def createdAfterCondition(threshold: Instant, isInclusive: Boolean = false) = creationCondition(threshold,
		if (isInclusive) LargerOrEqual else Larger)
	
	/**
	  * @param threshold Time threshold
	  * @param maxNumberOfItems Maximum number of items to return
	  * @param isInclusive Whether the threshold should be included in return values (default = false)
	  * @return Up to 'maxNumberOfItems' items that were created before the specified time threshold
	  */
	def createdBefore(threshold: Instant, maxNumberOfItems: Int, isInclusive: Boolean = false)(implicit connection: Connection) =
		take(maxNumberOfItems, defaultOrdering, Some(createdBeforeCondition(threshold, isInclusive)))
	
	/**
	  * @param threshold Time threshold
	  * @param isInclusive Whether the threshold should be included in return values (default = false)
	  * @param connection DB Connection (implicit)
	  * @return All items that were created after the specified time threshold
	  */
	def createdAfter(threshold: Instant, isInclusive: Boolean = false)(implicit connection: Connection) =
		getMany(createdAfterCondition(threshold, isInclusive))
	
	/**
	  * @param threshold A time threshold
	  * @param isInclusive Whether a possible item created exactly at the time threshold should be returned
	  * @param connection DB Connection (implicit)
	  * @return The last item that was created before the specified time threshold (None if no items were found)
	  */
	def latestBefore(threshold: Instant, isInclusive: Boolean = false)(implicit connection: Connection) =
		get(createdBeforeCondition(threshold, isInclusive))
	
	/**
	  * @param threshold A time threshold
	  * @param isInclusive Whether a possible item created exactly at the time threshold should be returned
	  * @param connection DB Connection (implicit)
	  * @return The first item that was created after the specified time threshold (None if no items were found)
	  */
	def firstAfter(threshold: Instant, isInclusive: Boolean = false)(implicit connection: Connection) =
		get(createdAfterCondition(threshold, isInclusive))
}
