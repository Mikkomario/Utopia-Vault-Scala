package utopia.vault.database

/**
 * Connection settings specify, how to connect to the database. A settings instance has value
 * semantics
 * @param connectionTarget
 * The new MariaDB / MySQL server to be used. Should not include 
 * the name of the database. Default: "jdbc:mysql://localhost:3306/"
 * @param user The user name used for accessing the database. Default: "root"
 * @param password the password used for accessing the database. Default: ""
 * @param driver The driver used when connecting to the server. Eg. "org.gjt.mm.mysql.Driver.". 
 * The mariaDB driver is used by default if nothing else is provided
 * @author Mikko Hilpinen
 * @since 16.4.2017
 */
case class ConnectionSettings(val connectionTarget: String = "jdbc:mysql://localhost:3306/", 
        val user: String = "root", val password: String = "", val defaultDBName: Option[String] = None, 
        val driver: Option[String] = None)