package usbbot.config

import ch.qos.logback.core.db.dialect.DBUtil
import com.mchange.v2.c3p0.ComboPooledDataSource
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import org.apache.commons.dbutils.AsyncQueryRunner
import org.apache.commons.dbutils.DbUtils
import org.apache.commons.dbutils.QueryRunner
import org.apache.commons.dbutils.ResultSetHandler
import sx.blah.discord.handle.obj.ICategory
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException
import java.sql.ResultSetMetaData
import java.sql.ResultSet
import java.util.*

data class Test(val name: String, val id: Long) {
}
object ResultSetHandlers {
    val guild = ResultSetHandler {
        if (!it.next()) {
            null
        } else {
            Guilds(it.getLong(1), it.getString(2))
        }
    }
    val guildIterable = ResultSetHandler {
        val collection = mutableListOf<Guilds>()
        while (it.next()) {
            collection.add(Guilds(it.getLong(1), it.getString(2)))
        }
        collection
    }
    val commandIterable = ResultSetHandler<Iterable<Command>> {
        val collection = mutableListOf<Command>()
        while (it.next()) {
            collection.add(Command(it.getLong(1), it.getLong(2), it.getString(3)))
        }
        collection
    }
}
object DatabaseConnectionKt {
    private val dataSource = ComboPooledDataSource()
    init {
        dataSource.jdbcUrl = "jdbc:postgresql://localhost:5432/ava"
        val properties = Properties()
        properties.setProperty("user", "postgres")
        properties.setProperty("password", "admin")
        dataSource.properties = properties

    }

    val queryRunner = QueryRunner(dataSource)

    fun close() {
        dataSource.close()
    }
}
class Guilds(val guildID: Long, val prefix: String) {
    fun getCommands() : Iterable<Command> {
        return DatabaseConnectionKt.queryRunner.
                query("SELECT * FROM commands WHERE guildID = ?",
                        ResultSetHandlers.commandIterable,
                        guildID)
    }
}

class Command(val ID: Long, val guildID: Long, val name: String)

fun main(args: Array<String>) {
    val dataSource = ComboPooledDataSource()
    dataSource.jdbcUrl = "jdbc:postgresql://localhost:5432/ava"
    val properties = Properties()
    properties.setProperty("user", "postgres")
    properties.setProperty("password", "admin")
    properties.setProperty("ssl", "false")
    dataSource.properties = properties
    val queryRunner = QueryRunner(dataSource)
    val startTime = System.currentTimeMillis()

    val data = Array<Array<Any>>(1_000_000) { i ->
        Array<Any>(2) {
            if (it == 0) {
                i
            } else{
                "!$i"
            }
        }
    }
    val dateCreationEndTime = System.currentTimeMillis()
    println("It took ${dateCreationEndTime - startTime}ms to create the data.")
    data.forEach {
        queryRunner.execute("INSERT INTO guilds (guildID, prefix) VALUES (?, ?)", it[0], it[1])
        println("Did stuff ${it[0]}")
    }
    //queryRunner.batch("INSERT INTO guilds (guildID, prefix) VALUES (?, ?)", data)


}

/*inline fun <T> Connection.easySQL(sql: String, block: (PreparedStatement) -> T) : T {
    this.use { conn ->
        conn.prepareStatement(sql).use { pstmt ->
            return block(pstmt)
        }
    }
}

fun isWatchedBeta(category: ICategory) : Boolean {
    DatabaseConnection.getConnection()
            .easySQL("SELECT * FROM watched_categories WHERE guildID = ? AND channelID = ?") {
                it.setLong(1, category.guild.longID)
                it.setLong(2, category.longID)
                return it.executeQuery().next()
            }
}*/

fun isWatched(category: ICategory?) : Boolean {
    if (category == null) return false
    val sql = "SELECT * FROM watched_categories WHERE guildID = ? AND channelID = ?"
    DatabaseConnection.getConnection().use { con ->
        con.prepareStatement(sql).use {
            it.setLong(1, category.guild.longID)
            it.setLong(2, category.longID)

            val rs = it.executeQuery()
            if (rs.next()) {
                return true
            }
        }
    }
    return false
}

fun addWatched(category: ICategory) : Boolean {
    val sql = "INSERT INTO watched_categories (guildID, channelID) VALUES (?, ?)"
    DatabaseConnection.getConnection().use { con ->
        con.prepareStatement(sql).use {
            it.setLong(1, category.guild.longID)
            it.setLong(2, category.longID)
            try {
                it.execute()
            } catch (ex: SQLException) {
                if (ex.errorCode == 19) {
                    return false
                } else {
                    throw ex
                }
            }
            return true
        }
    }
}

fun delWatched(category: ICategory) : Boolean {
    val sql = "DELETE FROM watched_categories WHERE guildID = ? AND channelID = ?"
    DatabaseConnection.getConnection().use { con ->
        con.prepareStatement(sql).use {
            it.setLong(1, category.guild.longID)
            it.setLong(2, category.longID)
            return 1 == it.executeUpdate()

        }
    }
}

fun getGuildCmdPrefix(guildID: Long) : String {
    val sql = "SELECT prefix FROM guilds WHERE guildID = ?"
    DatabaseConnection.getConnection().use { con ->
        con.prepareStatement(sql).use {
            it.setLong(1, guildID)
            val rs = it.executeQuery()
            if (rs.next()) {
                return rs.getString(1)
            } else {
                throw IllegalStateException("That guild dosen't exist in the database yet?")
            }
        }
    }
}

fun setGuildCmdPrefix(guildID: Long, newPrefix: String) : Boolean {
    val sql = "UPDATE guilds SET prefix = ? WHERE guildID = ?"
    DatabaseConnection.getConnection().use { con ->
        con.prepareStatement(sql).use {
            it.setString(1, newPrefix)
            it.setLong(2, guildID)
            return 1 == it.executeUpdate()
        }
    }
}