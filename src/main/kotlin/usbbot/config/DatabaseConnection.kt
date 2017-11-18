package usbbot.config

import com.mchange.v2.c3p0.ComboPooledDataSource
import org.apache.commons.dbutils.QueryRunner
import org.apache.commons.dbutils.ResultSetHandler
import usbbot.main.UsbBot

/**
 * The Object that Wraps the Connection to the Database Server
 */
object DatabaseConnection {
    private val dataSource = ComboPooledDataSource()
    init {
        dataSource.jdbcUrl = UsbBot.getProperty("DBURL")
        dataSource.user = UsbBot.getProperty("DBUser")
        dataSource.password = UsbBot.getProperty("DBPassword")

    }

    val queryRunner = QueryRunner(dataSource)

    fun close() {
        dataSource.close()
    }
}


/**
 * The class that is the base for all DatabaseEntry Classes
 */
abstract class DatabaseEntry {
    /**
     * Function that allows to delete the DatabaseEntry Contained by the current Object
     * @return the number of deleted rows
     */
    abstract fun delete() : Int
}



