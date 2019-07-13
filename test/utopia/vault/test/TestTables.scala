package utopia.vault.test

import utopia.vault.database.Tables

/**
 * This is a collection of tables used in the tests
 */
object TestTables
{
    // ATTRIBUTES   -----------------
    
    private val dbName = "vault_test"
    
    
    // COMPUTED ---------------------
    
    def person = Tables(dbName, "person")
    
    def strength = Tables(dbName, "strength")
    
    def indexTest = Tables(dbName, "index_test")
}