package usbbot.config;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class SimpleTextCommandsSQL {
	/**
	 * Gets all of the simple text usbbot.commands for a specified discord guild
	 * @param serverID id of the discord guild to get the usbbot.commands for
	 * @return a Map of name to command Text
	 */
	public static Map<String, String> getAllCommandsForServer(long serverID) {
		String sql = "SELECT commands.name, text_commands.text FROM text_commands, commands WHERE (guildID = ?) AND (text_commands.commandID = commands.ID)";
		HashMap<String, String> result = new HashMap<>();
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
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
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
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
	 */
	public static void insertCommand(long serverID, String name, String text) {
		int id = new CommandPermission(serverID, name).getCommandID();
		String sql = "INSERT INTO text_commands (commandID, text) VALUES (?, ?)";
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
			pstmt.setInt(1, id);
			pstmt.setString(2, text);
			pstmt.execute();
		} catch (SQLException ex) {
			System.err.println(ex.getMessage());
		}
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
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
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
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
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
