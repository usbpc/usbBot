package usbbot.config.updated

import com.mchange.v2.c3p0.ComboPooledDataSource
import org.apache.commons.dbutils.QueryRunner
import org.apache.commons.dbutils.ResultSetHandler
import java.util.*

/**
 * The Object that Wraps the Connection to the Database Server
 */
object DatabaseConnection {
    private val dataSource = ComboPooledDataSource()
    init {
        dataSource.jdbcUrl = "jdbc:postgresql://localhost:5432/ava"
        dataSource.user = "postgres"
        dataSource.password = "admin"

    }

    val queryRunner = QueryRunner(dataSource)

    fun close() {
        dataSource.close()
    }
}

/**
 * The Object that contains the The ResultSetHandlers for every Table/Request that dosen't have its own data class
 */
object MiscResultSetHandlers {
    val longCollection = ResultSetHandler {
        val output = mutableListOf<Long>()
        while (it.next()) {
            output.add(it.getLong(1))
        }
        output
    }
    val helptextHandler = ResultSetHandler<String?> {
        if (it.next()) {
            it.getString(1)
        } else {
            null
        }
    }
}

/**
 * The class that is the base for all DatabaseEntry Classes
 */
abstract class DatabaseEntry {
    /**
     * Function that allowes to delete the DatabaseEntry Contained by the current Object
     * @return the number of deleted rows
     */
    abstract fun delete() : Int
}

fun getHelptext(name: String) : String? =
        DatabaseConnection.queryRunner
                .query("SELECT helptext FROM command_helptext WHERE name = ?",
                        MiscResultSetHandlers.helptextHandler,
                        name)

fun getGuildById(guildID: Long) : Guild? =
        DatabaseConnection.queryRunner
                .query("SELECT * FROM guilds WHERE guildid = ?",
                        Guild.resultSetHandler,
                        guildID)

fun getCommandsForGuild(guildID: Long) : Collection<Command> =
        DatabaseConnection.queryRunner
                .query("SELECT * FROM commands WHERE guildid = ?",
                        Command.allCommandsHandler,
                        guildID)

fun getWatchedCategoriesForGuild(guildID: Long) : Collection<Long> =
        DatabaseConnection.queryRunner
                .query("SELECT channelid FROM watched_categories WHERE guildid = ?",
                        MiscResultSetHandlers.longCollection,
                        guildID)

open class Command(val ID: Int, val guildID: Long, val name: String, val rolemode: String, val usermode: String) : DatabaseEntry() {
    companion object {
        val allCommandsHandler = ResultSetHandler {
            val output = mutableListOf<Command>()
            while (it.next()) {
                output.add(
                        Command(
                                it.getInt(1),
                                it.getLong(2),
                                it.getString(3),
                                it.getString(4),
                                it.getString(5)))
            }
            output
        }
    }
    fun getPermissionRoles() : Collection<Long> =
            DatabaseConnection.queryRunner
                    .query("SELECT roleid FROM permissionRoles WHERE commandid = ?",
                            MiscResultSetHandlers.longCollection,
                            ID)

    fun getPermissionUsers() : Collection<Long> =
            DatabaseConnection.queryRunner
                    .query("SELECT roleid FROM permissionUsers WHERE commandid = ?",
                            MiscResultSetHandlers.longCollection,
                            ID)

    override fun delete() : Int =
            DatabaseConnection.queryRunner
                    .update("DELETE FROM commands WHERE commandid = ?",
                            ID)
}

class Guild(val guildID: Long, val prefix: String) : DatabaseEntry() {
    companion object {
        val resultSetHandler = ResultSetHandler<Guild?> {
            if (it.next()) {
                Guild(it.getLong(1), it.getString(2))
            } else {
                null
            }
        }
    }
    override fun delete(): Int =
        DatabaseConnection.queryRunner
                .update("DELETE FROM guilds WHERE guildid = ?",
                        guildID)

    fun getCommands() : Collection<Command> = getCommandsForGuild(guildID)

    fun getTextCommands() : Collection<TextCommand> =
            DatabaseConnection.queryRunner
                .query("SELECT * FROM commands INNER JOIN text_commands ON commands.id = text_commands.commandid WHERE guildid = ?",
                        TextCommand.textCommandHandler,
                        guildID)

    fun getWatchedCategories() : Collection<Long> = getWatchedCategoriesForGuild(guildID)
}

class TextCommand(ID: Int, guildID: Long, name: String, rolemode: String, usermode: String, val text: String) : Command(ID, guildID, name, rolemode, usermode) {
    companion object {
        val textCommandHandler = ResultSetHandler {
            val output = mutableListOf<TextCommand>()
            while (it.next()) {
                output.add(
                        TextCommand(
                                it.getInt(1),
                                it.getLong(2),
                                it.getString(3),
                                it.getString(4),
                                it.getString(5),
                                it.getString(6)))
            }
            output
        }
    }
}

