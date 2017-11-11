package usbbot.config;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.sqlite.SQLiteConfig;
import usbbot.main.UsbBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnection {
	private static Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);
	private static ComboPooledDataSource dataSource;
	public static Connection getConnection() {
		Connection con = null;
		if (dataSource == null)  {
			try {
				SQLiteConfig config = new SQLiteConfig();
				config.enforceForeignKeys(true);
				dataSource = new ComboPooledDataSource();
				String url = "jdbc:sqlite:" + UsbBot.sqlFile;
				logger.debug("Trying to connect with db url: {}", url);
				dataSource.setJdbcUrl(url);
				dataSource.setProperties(config.toProperties());
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
