package usbbot.config

import org.apache.commons.dbutils.ResultSetHandler

fun getGuildById(guildID: Long) : DBGuild? =
        DatabaseConnection.queryRunner
                .query("SELECT * FROM guilds WHERE guildid = ?",
                        DBGuild.resultSetHandler,
                        guildID)

fun setGuildPrefix(guildID: Long, newPrefix: String) = DatabaseConnection.queryRunner
            .update("UPDATE guilds SET prefix = ? WHERE guildid = ?",
                    newPrefix, guildID)


class DBGuild(val guildID: Long, val prefix: String) : DatabaseEntry() {
    companion object {
        val resultSetHandler = ResultSetHandler<DBGuild?> {
            if (it.next()) {
                DBGuild(it.getLong(1), it.getString(2))
            } else {
                null
            }
        }
    }

    override fun delete(): Int =
            DatabaseConnection.queryRunner
                    .update("DELETE FROM guilds WHERE guildid = ?",
                            guildID)

    fun getCommands() : Collection<DBCommand> = getCommandsForGuild(guildID)

    fun getTextCommands() : Collection<DBTextCommand> =
            getDBTextCommandsForGuild(guildID)


    fun getWatchedCategories() : Collection<Long> = getWatchedCategoriesForGuild(guildID)
}