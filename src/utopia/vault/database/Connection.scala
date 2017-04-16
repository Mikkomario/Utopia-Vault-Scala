package utopia.vault.database

import utopia.flow.generic.EnvironmentNotSetupException

object Connection
{
    /**
     * The settings used for establishing new connections. Must be specified before opening 
     * any database connections
     */
    var settings: Option[ConnectionSettings] = None
}

/**
 * Instances of this class handle database connections and allow low level database interaction 
 * through SQL statements
 * @author Mikko Hilpinen
 * @since 16.4.2017
 */
class Connection(initialDBName: String = "default")
{
    // ATTRIBUTES    -----------------
    
    private var _dbName = initialDBName
    /**
     * The name of the database the connection is used for
     */
    def dbName = _dbName
    def dbName_=(databaseName: String) = 
    {
        if (_dbName != databaseName)
        {
            _dbName = databaseName
            // Perform database switch if necessary
        }
    }
    
    private var _connection: Option[java.sql.Connection] = None
    
    
    // OTHER METHODS    -------------
    
    /**
     * Opens a new database connection. This is done automatically when the connection is used, too
     */
    @throws(classOf[EnvironmentNotSetupException])
    @throws(classOf[NoConnectionException])
    def open()
    {
        if (!_connection.exists { !_.isClosed() })
        {
            // Connection settings must be specified
            if (Connection.settings.isEmpty)
            {
                throw EnvironmentNotSetupException(
                        "Connection settings must be specified before a connection can be established")
            }
            
            try
            {
                // Sets up the driver
                // TODO: Should these driver instances be reused?
                Connection.settings.get.driver.foreach { Class.forName(_).newInstance() }
                
                // TODO: Continue by instantiating the connection
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
}