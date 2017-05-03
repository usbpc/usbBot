package commands.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PermissionManagerTest {
	@Test
	@DisplayName("Adding and Removing users")
	public void userTest() {
		PermissionManager manager = new PermissionManager();
		List<Long> roleList = new ArrayList<>();
		roleList.add(10L);
		roleList.add(20L);
		assertFalse(manager.hasPermission(12345L, roleList, "testCommand"), "Has Permission send true before anyone was added to any list");
		manager.addUser("testCommand", 12345L);
		assertTrue(manager.hasPermission(12345L, roleList, "testCommand"), "user was not sucessfully added to the permissions list");
		manager.delUser("testCommand", 12345L);
		assertFalse(manager.hasPermission(12345L, roleList, "testCommand"), "user was not successfully removed from the permissions list");

	}
	@Test
	@DisplayName("Adding and removing roles")
	public void rolesTest() {
		PermissionManager manager = new PermissionManager();
		List<Long> roleList = new ArrayList<>();
		roleList.add(10L);
		roleList.add(20L);

		assertFalse(manager.hasPermission(5550L, roleList, "testCommand"), "no role was added, yet true");
		manager.addRole("testCommand", 10L);
		assertTrue(manager.hasPermission(5550L, roleList, "testCommand"), "role was added, yet false");
		manager.delRole("testCommand", 10L);
		assertFalse(manager.hasPermission(5550L, roleList, "testCommand"), "role was removed, yet true");
	}
}