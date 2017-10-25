package config;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.sqlite.SQLiteConfig;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
	private static ComboPooledDataSource dataSource;
	public static Connection getConnection() {
		Connection con = null;
		if (dataSource == null)  {
			try {
				dataSource = new ComboPooledDataSource();
				dataSource.setJdbcUrl("jdbc:sqlite:configs/database.sqlite");
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}
		try {
			con = dataSource.getConnection();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return con;
	}
	public static void closeConnection() {
		if (dataSource == null) return;
		try {
			dataSource.close();
			dataSource = null;
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
}
