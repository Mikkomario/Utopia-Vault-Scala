package utopia.vault.sql

/**
 * Sql targets are suited targets for operations like select, update and delete. A target may 
 * consist of one or more tables and can always be converted to an sql segment when necessary
 */
trait SqlTarget
{
    // TODO: Add rightmostTable -value too for adding joins
    // - or maybe tables so that they can be used intelligently with references too
    
    /**
     * Converts this sql target into an sql segment
     */
    def toSqlSegment: SqlSegment
}