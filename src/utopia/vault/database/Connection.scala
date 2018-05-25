package utopia.vault.database

import utopia.flow.generic.EnvironmentNotSetupException
import java.sql.DriverManager
import java.sql.Statement
import java.sql.SQLException
import utopia.flow.datastructure.immutable.Value
import java.sql.PreparedStatement
import utopia.flow.parse.ValueConverterManager
import java.sql.Types
import java.sql.ResultSet
import utopia.vault.model.Table
import utopia.flow.datastructure.immutable.Model
import utopia.flow.datastructure.immutable.Constant
import scala.collection.immutable.HashSet
import utopia.vault.sql.SqlSegment
import utopia.vault.model.Result
import utopia.vault.model.Row
import scala.collection.immutable.VectorBuilder
import scala.util.Try
import utopia.flow.generic.IntType
import utopia.flow.generic.ValueConversions._
import utopia.vault.sql.SqlSegment

object Connection
{
    // ATTRIBUTES    ------------------------
    
    /**
     * The converter that converts values to sql compatible format
     */
    val sqlValueConverter = new ValueConverterManager(Vector(BasicSqlValueConverter))
    /**
     * The generator that converts sql data (object + type) into a value
     */
    val sqlValueGenerator = new SqlValueGeneratorManager(Vector(BasicSqlValueGenerator))
    
    /**
     * The settings used for establishing new connections
     */
    var settings = ConnectionSettings()
    
    // If an external driver is used in database operations, it is stored here after instantiation
    private var driver: Option[Any] = None
    
    
    // OTHER METHODS    ---------------------
    
    /**
     * Creates a temporary database connection for a specific operation. The connection is closed 
     * after the operation completes, even in error situations. No errors are catched though
     * @param dbName the name of the database that is used. Optional, and may be set later too.
     * @param f The function that is performed and which uses a database connection
     */
    def doTransaction[T](f: Connection => T) = 
    {
        val connection = new Connection()
        try 
        {
            f(connection)
        }
        finally
        {
            connection.close()
        }
    }
    
    /**
     * Creates a temporary database connection for a specific operation. The connection is closed 
     * after the operation completes. Any errors are catched and the resulting try reflects the 
     * success / failure state of the operation
     */
    def tryTransaction[T](f: Connection => T) = doTransaction(connection => { Try(f(connection)) })
}

/**
 * Instances of this class handle database connections and allow low level database interaction 
 * through SQL statements
 * @author Mikko Hilpinen
 * @since 16.4.2017
 */
class Connection(initialDBName: Option[String] = None)
{
    // ATTRIBUTES    -----------------
    
    private var _dbName = initialDBName
    /**
     * The name of the database the connection is used for. This is either defined by<br>
     * a) specifying the database name upon connection creation<br>
     * b) specified after the connection has been instantiated by assigning a new value<br>
     * c) the default option specified in the connection settings
     */
    def dbName = _dbName.orElse(Connection.settings.defaultDBName)
    def dbName_=(databaseName: String) = 
    {
        if (!dbName.exists { _ == databaseName })
        {
            // Performs a database change, if necessary
            if (isOpen && !_dbName.exists { _ == databaseName })
            {
                execute(s"USE $databaseName")
            }
            
            _dbName = Some(databaseName)
        }
    }
    
    private var _connection: Option[java.sql.Connection] = None
    private def connection = { open(); _connection.get }
    
    
    // COMPUTED PROPERTIES    -------
    
    /**
     * Whether the connection to the database has already been established
     */
    def isOpen = _connection.exists { !_.isClosed() }
    
    
    // OPERATORS    -----------------
    
    /**
     * Executes an sql statement and returns the results. The provided statement instance provides 
     * the exact parameters for the operation
     */
    def apply(statement: SqlSegment): Result = 
    {
        val selectedTables: Set[Table] = if (statement.isSelect) statement.targetTables else HashSet()
        
        // Changes database if necessary
        statement.databaseName.foreach { dbName = _ }
        apply(statement.sql, statement.values, selectedTables, statement.generatesKeys)
    }
    
    /**
     * Executes an sql query and returns the results. The provided values are injected to the 
     * query separately.
     * @param sql The sql string. Places for values are marked with '?'. There should always be 
     * exactly same number of these markers as there are values in the 'values' parameter
     * @param values The values that are injected to the query
     * @param selectedTables The tables for which resulting rows are parsed. If empty, the rows 
     * are not parsed at all.
     * @param returnGeneratedKeys Whether the resulting Result object should contain any keys 
     * generated during the query
     * @return The results of the query, containing the read rows and keys. If 'selectedTables' 
     * parameter was empty, no rows are included. If 'returnGeneratedKeys' parameter was false, 
     * no keys are included
     */
    def apply(sql: String, values: Seq[Value], selectedTables: Set[Table] = HashSet(), 
            returnGeneratedKeys: Boolean = false) = 
    {
        // Empty statements are not executed
        if (sql.isEmpty())
        {
            new Result(Vector())
        }
        else 
        {
            var statement: Option[PreparedStatement] = None
            var results: Option[ResultSet] = None
            try
            {
                // Creates the statement
                statement = Some(connection.prepareStatement(sql, 
                        if (returnGeneratedKeys) Statement.RETURN_GENERATED_KEYS 
                        else Statement.NO_GENERATED_KEYS));
                
                // Inserts provided values
                setValues(statement.get, values)
                
                // Executes the statement and retrieves the result
                results = Some(statement.get.executeQuery())
                
                // Parses data out of the result
                // May skip some data in case it is not requested
                new Result(if (selectedTables.isEmpty) Vector() else rowsFromResult(results.get, selectedTables), 
                        if (returnGeneratedKeys) generatedKeysFromResult(statement.get, selectedTables) else Vector())
            }
            finally
            {
                results.foreach { _.close() }
                statement.foreach { _.close() }
            }
        }
    }
    
    
    // OTHER METHODS    -------------
    
    /**
     * Tries to execute a statement. Wraps the results in a try
     */
    def tryExec(statement: SqlSegment) = Try(this(statement))
    
    /**
     * Opens a new database connection. This is done automatically when the connection is used, too
     */
    @throws(classOf[EnvironmentNotSetupException])
    @throws(classOf[NoConnectionException])
    def open()
    {
        // Only opens a new connection if there is no open connection available
        if (!isOpen)
        {   
            // Database name must be specified at this point
            if (dbName.isEmpty)
            {
                throw NoConnectionException("Database name hasn't been specified")
            }
            
            try
            {
                // Sets up the driver
                if (Connection.settings.driver.isDefined && Connection.driver.isEmpty)
                {
                    Connection.driver = Some(
                            Class.forName(Connection.settings.driver.get).newInstance())
                }
                
                // Instantiates the connection
                _connection = Some(DriverManager.getConnection(
                        Connection.settings.connectionTarget + dbName.get, 
                        Connection.settings.user, Connection.settings.password))
            }
            catch 
            {
                case e: Exception => throw NoConnectionException(
                        s"Failed to open a database connection with settings ${Connection.settings} and database '$dbName'", e)
            }
        }
    }
    
    /**
     * Closes this database connection. This should be called before the connection is discarded
     */
    def close()
    {
        try
        {
            _connection.foreach { _.close() }
            _connection = None
        }
        catch 
        {
            case _: Exception => // Exceptions here are ignored
        }
    }
    
    /**
     * Executes a simple sql string. Does not retrieve any values from the query.
     */
    @throws(classOf[EnvironmentNotSetupException])
    @throws(classOf[NoConnectionException])
    @throws(classOf[SQLException])
    def execute(sql: String) = 
    {
        // Empty statements are not executed
        if (!sql.isEmpty())
        {
            var statement: Option[Statement] = None
            try
            {
                statement = Some(connection.createStatement())
                statement.foreach { _.executeUpdate(sql) }
            }
            finally
            {
                statement.foreach { _.close() }
            }
        }
    }
    
    /**
     * Executes a query that allows use of prepared values. Reads and returns the resulting 
     * column data. Most of the time, using different versions of apply is better than using this method, 
     * but this one can be used without table data.
     * @param sql The sql string. Slots for values are indicated with question marks (?)
     * @param values the values inserted to the query. There should be a matching amount of values 
     * and slots in the sql string.
     * @return A map for each row. The map contains column name + column value pairs. Only non-null 
     * values are included.
     */
    @throws(classOf[EnvironmentNotSetupException])
    @throws(classOf[NoConnectionException])
    @throws(classOf[SQLException])
    def executeQuery(sql: String, values: Seq[Value] = Vector()) = 
    {
        // Empty statements are not executed
        if (sql.isEmpty())
        {
            Vector[Map[String, String]]()
        }
        else 
        {
            var statement: Option[PreparedStatement] = None
            var results: Option[ResultSet] = None
            try
            {
                // Creates the statement
                statement = Some(connection.prepareStatement(sql))
                
                // Inserts provided values
                setValues(statement.get, values)
                
                // Executes the statement and retrieves the result
                results = Some(statement.get.executeQuery())
                val meta = results.get.getMetaData
                
                val columnIndices = Vector.range(1, meta.getColumnCount + 1).map { index => 
                        (meta.getColumnName(index), index) }
                
                // Parses data out of the result
                val buffer = Vector.newBuilder[Map[String, String]]
                while (results.get.next())
                {
                    buffer += columnIndices.flatMap { case (name, index) => 
                            stringFromResult(results.get, index).map { (name, _) } }.toMap
                }
                
                buffer.result()
            }
            finally
            {
                results.foreach { _.close() }
                statement.foreach { _.close() }
            }
        }
    }
    
    private def setValues(statement: PreparedStatement, values: Seq[Value]) = 
    {
        for ( i <- 0 until values.size )
        {
            val conversionResult = Connection.sqlValueConverter(values(i))
            if (conversionResult.isDefined)
            {
                statement.setObject(i + 1, conversionResult.get._1, conversionResult.get._2)
            }
            else
            {
                statement.setNull(i + 1, Types.NULL)
            }
        }
    }
    
    private def stringFromResult(result: ResultSet, index: Int) = 
    {
        val value = result.getString(index)
        if (value == null) None else Some(value)
    }
    
    private def rowsFromResult(resultSet: ResultSet, tables: Iterable[Table]) = 
    {
        val meta = resultSet.getMetaData()
        
        // Sorts the column indices for targeted tables
        val indicesForTables = Vector.range(1, meta.getColumnCount + 1).groupBy { 
                index => tables.find { _.name == meta.getTableName(index) } }
        // Maps each index to a column in a targeted table, flattening the map as well
        // Resulting map: Table -> (Column, sqlType, index)
        val columnIndices = indicesForTables.flatMap { case (tableOption, indices) => 
                tableOption.map { table => (table, indices.flatMap { 
                index => (table.findColumnWithColumnName( meta.getColumnName(index) ).map { 
                (_, meta.getColumnType(index), index) }) }) } }
        
        // Parses the rows from the resultSet
        val rowBuffer = Vector.newBuilder[Row]
        while (resultSet.next())
        {
            // Reads the object data from each row, parses them into constants and creates a model 
            // The models are mapped to each table separately
            // NB: view.force is added in order to create a concrete map
            rowBuffer += new Row(columnIndices.mapValues { data => 
                new Model(data.map { case (column, sqlType, index) => new Constant(column.name, 
                Connection.sqlValueGenerator(resultSet.getObject(index), sqlType)) }) }.view.force)
        }
        
        rowBuffer.result()
    }
    
    private def generatedKeysFromResult(statement: Statement, tables: Traversable[Table]) = 
    {
        // Retrieves keys as ints if all of the tables (that use indexing) use int as key type
        val useInt = tables.forall { _.primaryColumn.forall { _.dataType == IntType } }
        val results = statement.getGeneratedKeys()
        val keyBuffer = Vector.newBuilder[Value]
        
        while (results.next())
        {
            val key: Value = if (useInt) results.getInt(1) else results.getLong(1)
            if (key.isDefined)
            {
                keyBuffer += key
            }
        }
        
        keyBuffer.result()
    }
}
