package config;

import org.sqlite.SQLiteConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
	private static Connection conn = null;
	public static Connection getConnection() {
		if (conn != null) {
			//System.out.println("Connection returned from cache!");
			return conn;
		} else {
			try {
				SQLiteConfig config = new SQLiteConfig();
				config.enforceForeignKeys(true);
				String url = "jdbc:sqlite:configs/database.sqlite";
				conn = DriverManager.getConnection(url, config.toProperties());
				//System.out.println(config.toProperties());

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
