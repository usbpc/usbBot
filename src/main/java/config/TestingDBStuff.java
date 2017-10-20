package config;

import java.sql.*;

public class TestingDBStuff {
	public static void main(String args[]) {
		insertCommand(104214741601247232L, "test", "This is a Test!");
		System.out.println(getCommandText(104214741601247232L, "test"));
		editCommand(104214741601247232L, "test", "Yay, another Test!");
		System.out.println(getCommandText(104214741601247232L, "test"));
		removeCommand(104214741601247232L, "test");
		System.out.println(getCommandText(104214741601247232L, "test"));
	}
	public static Connection connect() {
		Connection conn = null;
		try {
			String url = "jdbc:sqlite:configs/database.sqlite";
			conn = DriverManager.getConnection(url);

			System.out.println("Connection to SQLite has been established.");
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
		return conn;
	}
	public static String getCommandText(long serverID, String name) {
		String result = null;

		String sql = "SELECT text FROM text_commands WHERE (server_id = ?) AND (name = ?)";
		try (Connection conn = TestingDBStuff.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
	public static boolean insertCommand(long serverID, String name, String text) {
		boolean success = false;
		String sql = "INSERT INTO text_commands(server_id, name, text) VALUES (?,?,?)";
		try (Connection conn = TestingDBStuff.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setLong(1, serverID);
			pstmt.setString(2, name);
			pstmt.setString(3, text);
			success = pstmt.executeUpdate() >= 1;
		} catch (SQLException ex) {
			System.err.println(ex.getMessage());
		}
		return success;
	}

	public static boolean removeCommand(long serverID, String name) {
		String sql = "DELETE FROM text_commands WHERE (server_id = ?) AND (name = ?)";
		boolean success = false;
		try (Connection conn = TestingDBStuff.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setLong(1, serverID);
			pstmt.setString(2, name);
			success = pstmt.executeUpdate() >= 1;
		} catch (SQLException ex) {
			System.err.println(ex.getMessage());
		}
		return success;
	}

	public static boolean editCommand(long serverID, String name, String text) {
		String sql = "UPDATE text_commands SET text = ? WHERE (server_id = ?) AND (name = ?)";
		boolean success = false;
		try (Connection conn = TestingDBStuff.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
