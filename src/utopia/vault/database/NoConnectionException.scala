package utopia.vault.database

/**
 * These exceptions are thrown when connecting to database fails
 * @author Mikko Hilpinen
 * @since 16.4.2017
 */
case class NoConnectionException(val message: String, val cause: Throwable = null) extends 
        RuntimeException(message, cause)