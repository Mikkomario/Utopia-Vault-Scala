UTOPIA VAULT --------------------------------

Purpose
-------

    Utopia Vault is a framework for interacting with MariaDB / MySQL databases without having to write any SQL.
    Vault supports operations on high object oriented level, sql statement level and raw sql connection level.


Main Features
-------------

    Database connection and pooled connection handling
        - Using database connections is much more streamlined in Vault, which handles many repetitive and necessary
        background tasks like closing of result sets and changing database.
        - Query results are wrapped in immutable Result and Row classes, from which you can read all the data you need
        - Also, value insertion and statement preparation will be handled automatically for you

    SQL Statements with full support for typeless values and models
        - Vault uses Flow's Value and Model classes which means that all data types will be handled automatically
        under the hood.

    Template Statements that make database interactions much simpler and less prone to errors
        - Insert, Update, Delete, Select, SelectAll, Limit and OrderBy statements
        - Easy to write conditions with Where and Extensions
        - You don't need to know specific syntax for these statements. All you need to know is what they do and
        in which order to chain them.

    Automatic table structure and table reference reading
        - Use DatabaseTableReader and DatabaseReferenceReader to read table and reference data directly from the database
        - This means that you only need to update your database and all models will automatically reflect those changes.
            -> Table and reference structures can be stored in a single location.

    Advanced joining between tables using Join and SqlTarget
        - Once reference data has been set up (using DatabaseReferenceReader, for example), you can join tables
        together without specifying any columns or conditions. Vault will fill in all the blanks for you.
        - If you wish to manually specify joined columns, however, that is also possible

    Storable and Readable traits for object-oriented database interactions
        - Storable trait allows you to push (update or insert) model data to database with minimum syntax
        - Readable trait allows you to pull (read) up to date data from database to your model
        - Mutable DBModel class implements both of these traits
        - These traits allow you to use a MariaDB / MySQL server in a noSQL, object-oriented manner


Usage Notes
-----------

    Please call utopia.flow.generic.DataType.setup() before making any queries. Also, when adding values to queries
    and models, you can import utopia.flow.generic.ValueConversions._ for implicit value conversions.

    Unless you're using a local test database with root user and no password, please specify connection settings
    with Connection.settings = ConnectionSettings(...)

    The default driver option (None) in connection settings 'should' work if you've added mariadb-java-client-....jar
    to your build path / classpath. If not, you need to specify the name of the class you wish to use and make
    sure that class is included in the classpath.


Available Extensions
--------------------

    utopia.vault.sql.Extensions
        - Allows you to use values (or value convertible items) as condition elements
        - Usually works in combination with utopia.flow.generic.ValueConversions


v1.1  ------------------------------------

    New Features
    ------------

        ConnectionPool class for reusing connections and for connection sharing

        index(...) & indices(...) methods in Table allow to easily search for specific row indices

        Utility index methods added to Result and Row classes


    Changes & Updates
    -----------------

        Storable and StorableFactory traits now offer simple immutable model -based implementations when calling apply

        Made Result, Row Column and Table case classes

        Updated package structure
            - model divided into model.immutable and model.mutable
            - test package moved under a separate source

        Added `backticks` around column and table names within sql statements to avoid errors concerning reserved
        workds in MySQL

        Result and row index methods updated. Row index doesn't take parameters anymore and returns first index. Also,
        instead of returning Option[Value], the index methods now return Value (which may be empty)


    Fixes
    -----

        Fixed an error in DatabaseTableReader where table description syntax had changed
