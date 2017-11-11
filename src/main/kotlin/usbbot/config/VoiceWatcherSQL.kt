package usbbot.config

import sx.blah.discord.handle.obj.ICategory
import java.sql.SQLException

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