package commands.security;

import sx.blah.discord.handle.obj.IMessage;

import java.util.List;

public class Permission {
    private String roleMode;
    private List<Long> roles;

    private String userMode;
    private List<Long> users;

    private Permission() {
    }

    public Permission(String roleMode, List<Long> roles, String userMode, List<Long> users) {
        this.roleMode = roleMode;
        this.roles = roles;
        this.userMode = userMode;
        this.users = users;
    }
    List<Long> getRoles() {
        return roles;
    }
    String getRoleMode() {
        return roleMode;
    }
    void setRoleMode(String mode) {
        roleMode = mode;
    }
    void setUserMode(String mode) {
        userMode = mode;
    }
    String getUserMode() {
        return userMode;
    }
    List<Long> getUsers() {
        return users;
    }
    public void setUsers(List<Long> list) {
        users = list;
    }
    public void setRoles(List<Long> list) {
        roles = list;
    }

    public boolean isAllowed(IMessage message) {
        Long user = message.getAuthor().getLongID();

        if (users.contains(user) && userMode.equals("blacklist")) {
            return false;
        } else if (users.contains(user) && userMode.equals("whitelist")) {
            return true;
        }

        if (roleMode.equals("blacklist") && userMode.equals("whitelist") && roles.isEmpty() && users.isEmpty()) return false;
        if (message.getAuthor().getRolesForGuild(message.getGuild()).stream().filter(role -> roles.contains(role.getLongID())).count() > 0) {
            //If the user has any role that is mentioned in the "roles" list and the mode for roles is set to whitelist then the user is allowed to use the command
            if (roleMode.equals("whitelist")) {
                return true;
            }
        } else {
            //If the user had not any role that is mentioned in the "roles" list and the mode for roles is set to blacklist then the user is allowed to use the command
            if (roleMode.equals("blacklist")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return roleMode + roles.toString() + userMode + users.toString();
    }
}
