package usbbot.config

import com.mchange.v2.c3p0.ComboPooledDataSource
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
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
        dataSource.jdbcUrl = "jdbc:postgresql://localhost:5432/testDB"
        val properties = Properties()
        properties.setProperty("user", "postgres")
        properties.setProperty("password", "admin")
        properties.setProperty("ssl", "false")
        dataSource.properties = properties

    }
    val queryRunner = QueryRunner(dataSource)
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
    dataSource.jdbcUrl = "jdbc:postgresql://localhost:5432/testDB"
    val properties = Properties()
    properties.setProperty("user", "postgres")
    properties.setProperty("password", "admin")
    properties.setProperty("ssl", "false")
    dataSource.properties = properties
    val queryRunner = QueryRunner(dataSource)
    queryRunner.execute("DELETE FROM public.test WHERE true")
    val startTime = System.currentTimeMillis()
    val data = Array<Array<Any>>(1000000) { i ->
        Array<Any>(2) {
            if (it == 0) {
                i
            } else{
                "$i"
            }
        }
    }
    val dateCreationEndTime = System.currentTimeMillis()
    println("It took ${dateCreationEndTime - startTime}ms to create the data.")
    queryRunner.batch("INSERT INTO public.test (guildID, cmdPrefix) VALUES (?, ?)", data)



    val endTime = System.currentTimeMillis()
    println("It took ${endTime - dateCreationEndTime}ms to execute everything. That means it took ${(endTime - dateCreationEndTime)/1000000.0}ms per row inserted")
    queryRunner.execute("DELETE FROM public.test WHERE true")
    println("now starting corutine execution")
    val coroutineStart = System.currentTimeMillis()
    val deffered = (1..1000000L).map { i ->
        async {
            queryRunner.execute("INSERT INTO public.test (guildID, cmdPrefix) VALUES (?, ?)", i * 100L, "$i!")
        }
    }

    runBlocking {
        deffered.forEach { it.await() }
    }

    val corutineEnd = System.currentTimeMillis()
    println("It took ${corutineEnd - coroutineStart}ms to execute everything. That means it took ${(corutineEnd - coroutineStart)/1000000.0}ms per row inserted")

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
    val sql = "SELECT cmdPrefix FROM guilds WHERE guildID = ?"
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
    val sql = "UPDATE guilds SET cmdPrefix = ? WHERE guildID = ?"
    DatabaseConnection.getConnection().use { con ->
        con.prepareStatement(sql).use {
            it.setString(1, newPrefix)
            it.setLong(2, guildID)
            return 1 == it.executeUpdate()
        }
    }
}