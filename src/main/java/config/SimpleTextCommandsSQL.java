package config;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleTextCommandsSQL {
	public static void main(String args[]) {
		insertCommand(104214741601247232L, "test2", "This is a Test!");
		//editCommand(104214741601247232L, "test", "Update Command");
		getAllCommandsForServer(104214741601247232L).forEach((x, y) -> System.out.println(x + ":" +  y));
		System.out.println(getCommandText(104214741601247232L, "test"));
		//removeCommand(104214741601247232L, "test");
	}
	/**
	 * Gets all of the simple text commands for a specified discord guild
	 * @param serverID id of the discord guild to get the commands for
	 * @return a Map of name to command Text
	 */
	public static Map<String, String> getAllCommandsForServer(long serverID) {
		String sql = "SELECT commands.name, text_commands.text FROM text_commands, commands WHERE (guildID = ?) AND (text_commands.commandID = commands.ID)";
		HashMap<String, String> result = new HashMap<>();
		try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
			pstmt.setLong(1, serverID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				result.put(rs.getString("name"), rs.getString("text"));
			}
		} catch (SQLException ex) {
			System.err.println(ex.getMessage());
		}
		return result;
	}

	/**
	 * Gets the simple text command response text for a specified guildID and command name
	 * @param serverID the guildID
	 * @param name the command name
	 * @return if it exists the command text otherwise null
	 */
	public static String getCommandText(long serverID, String name) {
		String result = null;

		String sql = "SELECT text_commands.text FROM text_commands, commands WHERE (commands.guildID = ?) AND " +
				"(commands.name = ?) AND (text_commands.commandID = commands.ID)";
		try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
			pstmt.setLong(1, serverID);
			pstmt.setString(2, name);

			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				result = rs.getString(1);
			}

		} catch (SQLException ex) {
			System.err.println(ex.getMessage());
		}

		return result;
	}

	/**
	 * Add a new command for a specified server with the specified name and text if that name is not already in use on the server
	 * @param serverID the guildID
	 * @param name the command name to create
	 * @param text the text to add
	 * @return true if successful, false otherwise
	 */
	public static boolean insertCommand(long serverID, String name, String text) {
		boolean success = false;
		String sql = "INSERT INTO commands (guildID, name) VALUES (?,?)";
		try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
			pstmt.setString(2, name);
			pstmt.setLong(1, serverID);
			success = pstmt.executeUpdate() >= 1;

		} catch (SQLException ex) {
			System.err.println(ex.getMessage());
		}
		int id = 0;
		if (success) {
			sql = "SELECT ID FROM commands WHERE (guildID = ?) AND (name = ?)";
			try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
				pstmt.setLong(1, serverID);
				pstmt.setString(2, name);
				ResultSet rs = pstmt.executeQuery();
				if (rs.next()) {
					id = rs.getInt("ID");
				} else {
					success = false;
				}
			} catch (SQLException ex) {
				System.err.println(ex.getMessage());
			}
		}
		System.out.println("The id was:" + id);

		if (success) {
			sql = "INSERT INTO text_commands (commandID, text) VALUES (?, ?)";
			try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
				pstmt.setInt(1, id);
				pstmt.setString(2, text);
				success = pstmt.executeUpdate() >= 1;
			} catch (SQLException ex) {
				System.err.println(ex.getMessage());
			}
		}
		return success;
	}

	/**
	 * Removes the specified simple text command for the specified guildID
	 * @param serverID the guildID
	 * @param name the name of the command to delete
	 * @return true if successful, false otherwise
	 */
	public static boolean removeCommand(long serverID, String name) {
		String sql = "DELETE FROM commands WHERE (guildID = ?) AND (name = ?)";
		boolean success = false;
		try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
			pstmt.setLong(1, serverID);
			pstmt.setString(2, name);
			success = pstmt.executeUpdate() >= 1;
		} catch (SQLException ex) {
			System.err.println(ex.getMessage());
		}
		return success;
	}

	/**
	 * Edited the text for the specified command on the specified server
	 * @param serverID the guildID
	 * @param name the name of the command to update
	 * @param text the new text for the command
	 * @return true if successful, false otherwise
	 */
	public static boolean editCommand(long serverID, String name, String text) {
		String sql = "UPDATE text_commands SET text = ? WHERE (commandID = (SELECT ID FROM commands WHERE guildID = ? AND name = ?))";
		boolean success = false;
		try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
			pstmt.setString(1, text);
			pstmt.setLong(2, serverID);
			pstmt.setString(3, name);
			success = pstmt.executeUpdate() >= 1;
		} catch (SQLException ex) {
			System.err.println(ex.getMessage());
		}

		return success;
	}

}
