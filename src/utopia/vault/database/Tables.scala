package utopia.vault.database

import utopia.vault.model.mutable.References

import scala.collection.immutable.HashMap

/**
  * Keeps track & setups of all database tables and their references by reading data directly from the database.
  * @author Mikko Hilpinen
  * @since 13.7.2019, v1.3+
  */
object Tables
{
	// ATTRIBUTES	---------------------
	
	private var dbs = HashMap[String, TablesReader]()
	private var _tableNameConversion: String => String = DatabaseTableReader.underlineToCamelCase
	
	
	// COMPUTED	-------------------------
	
	/**
	 * @return The method used when converting table names in the database to table names in code (by default converts
	 *         from underscore style to camel case style, eg. "table_name" to "tableName")
	 */
	def tableNameConversion = _tableNameConversion
	/**
	 * Specifies a new name conversion style for tables read from the DB
	 * @param newConversionMethod A new method for table name conversion. Takes the database-originated table name
	 *                            as parameter and returns the table name used in the code.
	 */
	def tableNameConversion_=(newConversionMethod: String => String) =
	{
		// Has to clear all existing data to use the new method
		_tableNameConversion = newConversionMethod
		dbs.keys.foreach(References.clear)
		dbs = HashMap()
	}
	
	
	// OPERATORS	---------------------
	
	/**
	  * Finds data for a specific table. Initializes database table data if necessary.
	  * @param dbName Database name
	  * @param tableName Table name
	  * @return That table's data
	  */
	def apply(dbName: String, tableName: String) = reader(dbName)(tableName.toLowerCase)
	
	
	// OTHER	-----------------------
	
	/**
	  * @param dbName Targeted database name
	  * @return All tables in the specified database
	  */
	def all(dbName: String) = reader(dbName).tables.values
	
	private def reader(dbName: String) =
	{
		val lowerDbName = dbName.toLowerCase
		
		if (dbs.contains(lowerDbName))
			dbs(lowerDbName)
		else
		{
			// May have to initialize new databases
			val db = new TablesReader(dbName)
			dbs += lowerDbName -> db
			db
		}
	}
}

private class TablesReader(val dbName: String)
{
	// ATTRIBUTES	-------------------
	
	val tables =
	{
		Connection.doTransaction { implicit connection =>
			
			connection.dbName = dbName
			
			// First finds out table names using "show tables"
			val tableNames = connection.executeQuery("show tables").flatMap { _.values.headOption }
			
			// Reads data for each table
			val tables = tableNames.map { DatabaseTableReader(dbName, _) }
			
			// Sets up references between the tables
			DatabaseReferenceReader.setupReferences(tables.toSet)
			
			tables.map { t => t.name.toLowerCase -> t }.toMap
		}
	}
	
	
	// OPERATORS	-------------------
	
	def apply(tableName: String) =
	{
		if (tables.contains(tableName))
			tables(tableName)
		else
			throw new NoSuchTableException(dbName, tableName)
	}
}

private class NoSuchTableException(dbName: String, tableName: String) extends RuntimeException(
	s"Database $dbName doesn't contain a table named $tableName")