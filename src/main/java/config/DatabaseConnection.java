package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
	private static Connection conn = null;
	public static Connection getConnection() {
		if (conn != null) {
			//System.out.println("Connection returned from cache!");
			return conn;
		} else {
			try {
				String url = "jdbc:sqlite:configs/database.sqlite";
				conn = DriverManager.getConnection(url);

				//System.out.println("Connection to SQLite has been established.");
			} catch (SQLException e) {
				System.err.println(e.getMessage());
			}
			return conn;
		}
	}
	public static void closeConnection() {
		if (conn == null) return;
		try {
			conn.close();
			conn = null;
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
	}
}
