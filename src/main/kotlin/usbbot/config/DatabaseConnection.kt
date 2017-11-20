package usbbot.config

import com.mchange.v2.c3p0.ComboPooledDataSource
import org.apache.commons.dbutils.QueryRunner
import org.apache.commons.dbutils.ResultSetHandler
import usbbot.main.UsbBot
import java.util.*

/**
 * The Object that Wraps the Connection to the Database Server
 */
object DatabaseConnection {
    private val dataSource = ComboPooledDataSource()
    init {
        val prop = Properties()
        prop.setProperty("tcpKeepAlive", "true")
        prop.setProperty("ApplicationName", "Ava Discord Bot")
        prop.setProperty("password", UsbBot.getProperty("DBPassword"))
        prop.setProperty("user", UsbBot.getProperty("DBUser"))
        dataSource.jdbcUrl = UsbBot.getProperty("DBURL")
        dataSource.properties = prop


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