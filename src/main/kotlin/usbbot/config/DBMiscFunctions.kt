package usbbot.config

import org.apache.commons.dbutils.ResultSetHandler

fun getHelptext(name: String) : String? =
        DatabaseConnection.queryRunner
                .query("SELECT helptext FROM command_helptext WHERE name = ?",
                        MiscResultSetHandlers.helptextHandler,
                        name)

fun getWatchedCategoriesForGuild(guildID: Long) : Collection<Long> =
        DatabaseConnection.queryRunner
                .query("SELECT channelid FROM watched_categories WHERE guildid = ?",
                        MiscResultSetHandlers.longCollection,
                        guildID)

fun delWatchedForGuild(guildID: Long, category: Long) =
        DatabaseConnection.queryRunner
                .update("DELETE FROM watched_categories WHERE guildid = ? AND channelid = ?",
                        guildID, category)

fun addWatchedForGuild(guildID: Long, category: Long) =
        DatabaseConnection.queryRunner
                .update("INSERT INTO watched_categories (guildid, channelid) VALUES (?, ?)",
                        guildID, category)

fun isWached(guildID: Long, category: Long) : Int =
        DatabaseConnection.queryRunner
                .query("SELECT COUNT(*) FROM watched_categories WHERE guildid = ? AND channelid = ?",
                        MiscResultSetHandlers.singleInt,
                        guildID, category)
/**
 * The Object that contains the The ResultSetHandlers for every Table/Request that dosen't have its own data class
 */
object MiscResultSetHandlers {
    val singleInt = ResultSetHandler<Int> {
        it.next()
        it.getInt(1)
    }
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