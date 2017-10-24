package config;

import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;

public class MiscSQLCommand {
	private static Logger logger = LoggerFactory.getLogger(MiscSQLCommand.class);
	public static boolean commandExists(long guildID, String name) {
		String sql = "SELECT * FROM commands WHERE (guildID = ?) AND (name = ?)";
		try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
		pstmt.setLong(1, guildID);
		pstmt.setString(2, name);
		ResultSet rs = pstmt.executeQuery();
		if (rs.next()) {
			logger.debug("Returning true");
			return true;
		}
		logger.debug("Returning false");
		} catch (SQLException ex) {
			logger.error(ex.getMessage());
		}
		return false;
	}
	public static @Nullable String getHelpText(String name) {
		String retVal = null;
		String sql = "SELECT helptext FROM command_helptext WHERE (name = ?)";
		try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
			pstmt.setString(1, name);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				retVal = rs.getString(1);
			}
		} catch (SQLException ex) {
			logger.error(ex.getMessage());
		}
		return retVal;
	}
}
