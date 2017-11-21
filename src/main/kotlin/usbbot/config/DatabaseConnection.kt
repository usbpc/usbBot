package usbbot.config

import com.zaxxer.hikari.HikariDataSource
import org.apache.commons.dbutils.QueryRunner
import org.apache.commons.dbutils.ResultSetHandler
import usbbot.main.UsbBot
import java.util.*

/**
 * The Object that Wraps the Connection to the Database Server
 */
object DatabaseConnection {
    val dataSource = HikariDataSource()
    init {
        dataSource.password=  UsbBot.getProperty("DBPassword")
        dataSource.username = UsbBot.getProperty("DBUser")
        dataSource.jdbcUrl = UsbBot.getProperty("DBURL")

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