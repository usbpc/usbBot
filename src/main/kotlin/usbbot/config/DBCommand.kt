package usbbot.config

import org.apache.commons.dbutils.ResultSetHandler

fun getCommandForGuild(guildID: Long, name: String) : DBCommand? =
        DatabaseConnection.queryRunner
                .query("SELECT * FROM commands WHERE guildid = ? AND name = ?",
                        DBCommand.singleCommandHandler,
                        guildID, name)

fun getCommandsForGuild(guildID: Long) : Collection<DBCommand> =
        DatabaseConnection.queryRunner
                .query("SELECT * FROM commands WHERE guildid = ?",
                        DBCommand.allCommandsHandler,
                        guildID)

fun createDBCommand(guildID: Long, name: String, rolemode: String, usermode: String) : DBCommand {
    val ID = DatabaseConnection.queryRunner
            .insert("INSERT INTO commands (guildid, name, rolemode, usermode) VALUES (?, ?, ?, ?)",
                    DBCommand.insertHandler,
                    guildID, name, rolemode, usermode) ?: throw IllegalStateException("ID shall not be NULL here")

    return DBCommand(ID, guildID, name, rolemode, usermode)
}

open class DBCommand(val ID: Int, val guildID: Long, val name: String, var rolemode: String, var usermode: String) : DatabaseEntry() {

    companion object {
        val insertHandler = ResultSetHandler {
            if (it.next()) {
                it.getInt(1)
            } else {
                null
            }
        }
        val allCommandsHandler = ResultSetHandler {
            val output = mutableListOf<DBCommand>()
            while (it.next()) {
                output.add(
                        DBCommand(
                                it.getInt(1),
                                it.getLong(2),
                                it.getString(3),
                                it.getString(4),
                                it.getString(5)))
            }
            output
        }

        val singleCommandHandler = ResultSetHandler {
            if (it.next()) {
                DBCommand(
                        it.getInt(1),
                        it.getLong(2),
                        it.getString(3),
                        it.getString(4),
                        it.getString(5))
            } else {
                null
            }
        }
    }

    fun getPermissionRoles() : Collection<Long> =
            DatabaseConnection.queryRunner
                    .query("SELECT roleid FROM permissionRoles WHERE commandid = ?",
                            MiscResultSetHandlers.longCollection,
                            ID)

    fun getPermissionUsers() : Collection<Long> =
            DatabaseConnection.queryRunner
                    .query("SELECT userid FROM permissionUsers WHERE commandid = ?",
                            MiscResultSetHandlers.longCollection,
                            ID)

    fun addUserToList(userID: Long) : Int =
            DatabaseConnection.queryRunner
                    .update("INSERT INTO permissionusers (commandid, userid) values (?, ?)",
                            ID, userID)

    fun delUserFromList(userID: Long) : Int =
            DatabaseConnection.queryRunner
                    .update("DELETE FROM permissionusers WHERE commandid = ? AND userid = ?",
                            ID, userID)

    fun addRoleToList(roleID: Long) : Int =
            DatabaseConnection.queryRunner
                    .update("INSERT INTO permissionroles (commandid, roleid) values (?, ?)",
                            ID, roleID)

    fun delRoleFromList(roleID: Long) : Int =
            DatabaseConnection.queryRunner
                    .update("DELETE FROM permissionroles WHERE commandid = ? AND roleid = ?",
                            ID, roleID)

    fun setRoleMode(roleMode: String) : Int {
        rolemode = roleMode
        return DatabaseConnection.queryRunner
                .update("UPDATE commands SET rolemode = ? WHERE commandid = ?",
                        roleMode, ID)
    }

    fun setUserMode(userMode: String) : Int {
        usermode = userMode
        return DatabaseConnection.queryRunner
                .update("UPDATE commands SET usermode = ? WHERE commandid = ?",
                        userMode, ID)
    }


    override fun delete() : Int =
            DatabaseConnection.queryRunner
                    .update("DELETE FROM commands WHERE id = ?",
                            ID)
}