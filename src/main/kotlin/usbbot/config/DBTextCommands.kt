package usbbot.config

import org.apache.commons.dbutils.ResultSetHandler

fun createDBTextCommand(guildID: Long, name: String, rolemode: String, usermode: String, text: String) : DBTextCommand {
    val cmd = createDBCommand(guildID, name, rolemode, usermode)

    DatabaseConnection.queryRunner
            .update("INSERT INTO text_commands (commandid, text) VALUES (?, ?)",
                    cmd.ID, text)

    return DBTextCommand(cmd, text)
}

fun getDBTextCommand(guildID: Long, name: String) =
        DatabaseConnection.queryRunner
                .query("SELECT id, guildid, name, rolemode, usermode, text " +
                        "FROM commands " +
                        "INNER JOIN text_commands ON commands.id = text_commands.commandid " +
                        "WHERE guildid = ? and name = ?",
                        DBTextCommand.textCommandHandler,
                        guildID, name)


fun getDBTextCommandsForGuild(guildID: Long) : Collection<DBTextCommand> =
        DatabaseConnection.queryRunner
                .query("SELECT id, guildid, name, rolemode, usermode, text FROM commands " +
                        "INNER JOIN text_commands ON commands.id = text_commands.commandid " +
                        "WHERE guildid = ?",
                        DBTextCommand.textCommandsHandler,
                        guildID)

class DBTextCommand(ID: Int, guildID: Long, name: String, rolemode: String, usermode: String, var text: String) : DBCommand(ID, guildID, name, rolemode, usermode) {

    constructor(dbCommand: DBCommand, text: String) :
            this(dbCommand.ID, dbCommand.guildID, dbCommand.name, dbCommand.rolemode, dbCommand.usermode, text)

    fun editText(newText: String) {
        text = newText

        DatabaseConnection.queryRunner
                .update("UPDATE text_commands SET text = ? WHERE comandid = ?",
                        newText, ID)
    }

    companion object {
        val textCommandHandler = ResultSetHandler<DBTextCommand?> {
            if (it.next()) {
                DBTextCommand(
                        it.getInt(1),
                        it.getLong(2),
                        it.getString(3),
                        it.getString(4),
                        it.getString(5),
                        it.getString(6))
            } else {
                null
            }
        }
        val textCommandsHandler = ResultSetHandler {
            val output = mutableListOf<DBTextCommand>()
            while (it.next()) {
                output.add(
                        DBTextCommand(
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