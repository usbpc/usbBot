package config

import modules.MiscCommands
import org.slf4j.LoggerFactory
import sx.blah.discord.handle.obj.ICategory
import java.sql.Connection
import java.sql.PreparedStatement
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