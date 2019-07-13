package utopia.vault.database

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
	
	
	// OPERATORS	---------------------
	
	/**
	  * Finds data for a specific table. Initializes database table data if necessary.
	  * @param dbName Database name
	  * @param tableName Table name
	  * @return That table's data
	  */
	def apply(dbName: String, tableName: String) =
	{
		val lowerDbName = dbName.toLowerCase
		val lowerTableName = tableName.toLowerCase
		
		if (dbs.contains(lowerDbName))
			dbs(lowerDbName)(lowerTableName)
		else
		{
			// May have to initialize new databases
			val db = new TablesReader(dbName)
			dbs += lowerDbName -> db
			db(lowerTableName)
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