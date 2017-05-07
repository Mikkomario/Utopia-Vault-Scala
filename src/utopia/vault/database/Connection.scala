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
import utopia.vault.generic.Table

object Connection
{
    /**
     * The converter that converts values to sql compatible format
     */
    val sqlValueConverter = new ValueConverterManager(Vector(BasicSqlValueConverter))
    /**
     * The generator that converts sql data (object + type) into a value
     */
    val sqlValueGenerator = new SqlValueGeneratorManager(Vector(BasicSqlValueGenerator))
    
    /**
     * The settings used for establishing new connections. Must be specified before opening 
     * any database connections
     */
    var settings: Option[ConnectionSettings] = None
    
    // If an external driver is used in database operations, it is stored here after instantiation
    private var driver: Option[Any] = None
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
    def dbName = _dbName.orElse(Connection.settings.flatMap { _.defaultDBName })
    def dbName_=(databaseName: String) = 
    {
        if (!dbName.exists { _ == databaseName })
        {
            _dbName = Some(databaseName)
            // Perform database switch if necessary
        }
    }
    
    private var _connection: Option[java.sql.Connection] = None
    private def connection = { open(); _connection.get }
    
    
    // COMPUTED PROPERTIES    -------
    
    /**
     * Whether the connection to the database has already been established
     */
    def isOpen = _connection.exists { !_.isClosed() }
    
    
    // OTHER METHODS    -------------
    
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
            // Connection settings must be specified
            if (Connection.settings.isEmpty)
            {
                throw EnvironmentNotSetupException(
                        "Connection settings must be specified before a connection can be established")
            }
            
            // Database name must be specified at this point
            if (dbName.isEmpty)
            {
                throw NoConnectionException("Database name hasn't been specified")
            }
            
            try
            {
                // Sets up the driver
                if (Connection.settings.get.driver.isDefined && Connection.driver.isEmpty)
                {
                    Connection.driver = Some(
                            Class.forName(Connection.settings.get.driver.get).newInstance())
                }
                
                // Instantiates the connection
                _connection = Some(DriverManager.getConnection(
                        Connection.settings.get.connectionTarget + dbName.get, 
                        Connection.settings.get.user, Connection.settings.get.password))
            }
            catch 
            {
                case e: Exception => throw NoConnectionException(
                        s"Failed to open a database connection with settings ${Connection.settings.get} and database $dbName", e)
            }
        }
    }
    
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
    
    @throws(classOf[EnvironmentNotSetupException])
    @throws(classOf[NoConnectionException])
    @throws(classOf[SQLException])
    def executeSimple(sql: String) = 
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
    
    def execute(sql: String, values: Vector[Value], selectedTables: Vector[Table] = Vector(), 
            returnGeneratedKeys: Boolean = false) = 
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
            for ( i <- 0 until values.size )
            {
                val conversionResult = Connection.sqlValueConverter(values(i))
                if (conversionResult.isDefined)
                {
                    statement.get.setObject(i + 1, conversionResult.get._1, conversionResult.get._2)
                }
                else
                {
                    // TODO: How to get the correct data type for the null value?
                    // May possibly need to add a helper class for this
                    statement.get.setNull(i + 1, Types.NULL)
                }
            }
            
            // Executes the statement and retrieves the result
            results = Some(statement.get.executeQuery())
        }
        finally
        {
            results.foreach { _.close() }
            statement.foreach { _.close() }
        }
    }
    
    private def rowsFromResult(resultSet: ResultSet, tables: Iterable[Table]) = 
    {
        val meta = resultSet.getMetaData()
        
    }
}
