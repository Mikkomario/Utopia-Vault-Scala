package utopia.vault.database

/**
 * Connection settings specify, how to connect to the database. A settings instance has value
 * semantics
 * @param connectionTarget
 * The new MariaDB / MySQL server to be used. Should not include 
 * the name of the database. For example: "jdbc:mysql://localhost:3306/"
 * @param driver The driver used when connecting to the server. Eg. "org.gjt.mm.mysql.Driver."
 * @author Mikko Hilpinen
 * @since 16.4.2017
 */
case class ConnectionSettings(val connectionTarget: String, val user: String, 
        val password: String, val driver: Option[String] = None)