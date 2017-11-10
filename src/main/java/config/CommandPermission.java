package config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.crypto.Data;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;

//TODO: Deal with when the DB is not available or has an error... right now we just kinda ignore it and continue...
@SuppressWarnings("Duplicates")
public class CommandPermission {
	private static Logger logger = LoggerFactory.getLogger(CommandPermission.class);
	private long guildID;
	private String name;
	private int commandID;
	private boolean userModeIsBlacklist;
	private boolean roleModeIsBlacklist;
	public CommandPermission(long guildID, String name) {
		this.guildID = guildID;
		this.name = name;
		if (getCommandId()) {
			getListModes();
		} else {
			createDefaultEntry();
		}
	}
	public boolean isUserModeBlacklist() {
		return userModeIsBlacklist;
	}
	public boolean isRoleModeBlacklist() {
		return roleModeIsBlacklist;
	}
	/**
	 * Gets the internal ID for a command on a specific guild if it exists.
	 * @return true if there is an ID, false otherwise.
	 */
	private boolean getCommandId() {
		String sql = "SELECT ID FROM commands WHERE (guildID = ?) AND (name = ?)" ;
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
			pstmt.setLong(1, guildID);
			pstmt.setString(2, name);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				this.commandID = rs.getInt(1);
				logger.debug("Command: {} for Guild {} has internal id {}", name, guildID, commandID);
				return true;
			} else {
				logger.debug("Command: {} for Guild {} has no internal id yet.", name, guildID);
				return false;
			}
		} catch (SQLException ex) {
			logger.error(ex.getMessage());
		}
		return false;
	}
	private void getListModes() {
		String sql = "SELECT roleMode, userMode FROM permissions  WHERE (commandID = ?)";
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
			pstmt.setInt(1, commandID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				this.roleModeIsBlacklist = rs.getString(1).equals("blacklist");
				this.userModeIsBlacklist = rs.getString(2).equals("blacklist");
				logger.debug("Command: {} for Guild {} has internal id {} roleMode: {} userMode {}",
						name, guildID, commandID, rs.getString(1), rs.getString(2));
			} else {
				logger.debug("Command: {} for Guild {} dosen't have permissions yet.", name, guildID);
			}
		} catch (SQLException ex) {
			logger.error(ex.getMessage());
		}
	}

	private void createDefaultEntry() {
		String sql = "INSERT INTO commands (guildID, name) VALUES (?, ?)";
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
			pstmt.setLong(1, guildID);
			pstmt.setString(2, name);
			pstmt.executeUpdate();
		} catch (SQLException ex) {
			logger.error(ex.getMessage());
		}
		sql = "SELECT ID FROM commands WHERE (guildID = ?) AND (name = ?)";
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
			pstmt.setLong(1, guildID);
			pstmt.setString(2, name);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				commandID = rs.getInt(1);
			}
		} catch (SQLException ex) {
			logger.error(ex.getMessage());
		}
		sql = "INSERT INTO permissions (commandID, roleMode, userMode) VALUES (?, ?, ?)";
		userModeIsBlacklist = roleModeIsBlacklist = false;
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
			pstmt.setInt(1, commandID);
			pstmt.setString(2, "whitelist");
			pstmt.setString(3, "whitelist");
			pstmt.executeUpdate();
		} catch (SQLException ex) {
			logger.error(ex.getMessage());
		}
	}

	public void setRoleMode(boolean toBlacklist) {
		String sql = "UPDATE permissions SET roleMode = ? WHERE (commandID = ?)";
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
			pstmt.setString(1, (toBlacklist ? "blacklist" : "whitelist"));
			pstmt.setInt(2, commandID);
			pstmt.executeUpdate();
		} catch (SQLException ex) {
			logger.error(ex.getMessage());
		}
	}

	public void setUserMode(boolean toBlacklist) {
		String sql = "UPDATE permissions SET userMode =  ? WHERE (commandID = ?)";
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
			pstmt.setString(1, (toBlacklist ? "blacklist" : "whitelist"));
			pstmt.setInt(2, commandID);
			pstmt.executeUpdate();
		} catch (SQLException ex) {
			logger.error(ex.getMessage());
		}
	}

	public void addUser(long userID) {
		String sql = "INSERT INTO permissionUsers (commandID, userID) VALUES (?, ?)";
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
			pstmt.setInt(1, commandID);
			pstmt.setLong(2, userID);
			pstmt.executeUpdate();
		} catch (SQLException ex) {
			logger.error(ex.getMessage());
		}
	}
	public void addRole(long roleID) {
		String sql = "INSERT INTO permissionRoles (commandID, roleID) VALUES (?, ?)";
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
			pstmt.setInt(1, commandID);
			pstmt.setLong(2, roleID);
			pstmt.executeUpdate();
		} catch (SQLException ex) {
			logger.error(ex.getMessage());
		}
	}
	public boolean delUser(long userID) {
		boolean success = false;
		String sql = "DELETE FROM permissionUsers WHERE (commandID = ?) AND (userID = ?)";
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
			pstmt.setInt(1, commandID);
			pstmt.setLong(2, userID);
			success = pstmt.executeUpdate() >= 1;
		} catch (SQLException ex) {
			logger.error(ex.getMessage());
		}
		return success;
	}
	public boolean delRole(long roleID) {
		boolean success = false;
		String sql = "DELETE FROM permissionRoles WHERE (commandID = ?) AND (roleID = ?)";
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
			pstmt.setInt(1, commandID);
			pstmt.setLong(2, roleID);
			success = pstmt.executeUpdate() >= 1;
		} catch (SQLException ex) {
			logger.error(ex.getMessage());
		}
		return success;
	}
	public boolean containsUser(long userID) {
		String sql = "SELECT * FROM permissionUsers WHERE (commandID = ?) AND (userID = ?)";
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
			pstmt.setInt(1, commandID);
			pstmt.setLong(2, userID);
			ResultSet rs = pstmt.executeQuery();
			return rs.next();
		} catch (SQLException ex) {
			logger.error(ex.getMessage());
		}
		return false;
	}
	public boolean containsAnyRole(Collection<Long> roleIDs) {
		String sql = "SELECT roleID FROM permissionRoles WHERE (commandID = ?)";
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
			pstmt.setInt(1, commandID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				if (roleIDs.contains(rs.getLong(1))) {
					return true;
				}
			}
			return false;
		} catch (SQLException ex) {
			logger.error(ex.getMessage());
		}
		return false;
	}
	public Collection<Long> getAllUserIDs() {
		HashSet<Long> userIDs = new HashSet<>();
		String sql = "SELECT userID FROM permissionUsers WHERE (commandID = ?)";
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
			pstmt.setInt(1, commandID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				userIDs.add(rs.getLong(1));
			}
		} catch (SQLException ex) {
			logger.error(ex.getMessage());
		}
		return userIDs;
	}

	public Collection<Long> getAllRoleIDs() {
		HashSet<Long> roleIDs = new HashSet<>();
		String sql = "SELECT roleID FROM permissionRoles WHERE (commandID = ?)";
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
			pstmt.setInt(1, commandID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				roleIDs.add(rs.getLong(1));
			}
		} catch (SQLException ex) {
			logger.error(ex.getMessage());
		}
		return roleIDs;
	}

	public boolean anyListPopulated() {
		String sql = "SELECT * FROM permissionRoles WHERE (commandID = ?)";
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
			pstmt.setInt(1, commandID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				return true;
			}
		} catch (SQLException ex) {
			logger.error(ex.getMessage());
		}
		sql = "SELECT * FROM permissionUsers WHERE (commandID = ?)";
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {
			pstmt.setInt(1, commandID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				return true;
			}
		} catch (SQLException ex) {
			logger.error(ex.getMessage());
		}
		return false;
	}
}
