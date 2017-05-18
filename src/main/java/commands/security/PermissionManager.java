package commands.security;

import commands.DiscordCommands;
import commands.core.Command;
import util.commands.AnnotationExtractor;
import util.commands.DiscordCommand;
import util.commands.DiscordSubCommand;
import config.Config;
import config.ConfigElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MessageParsing;
import util.MessageSending;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

import java.util.*;

public class PermissionManager implements DiscordCommands {
    private static Logger logger = LoggerFactory.getLogger(PermissionManager.class);
    private Map<String, DummyCommand> commandMap = new HashMap<>();
    public PermissionManager() {
        Config.getConfigByName("permissions").getAllObjectsAs(DummyCommand.class).forEach(x -> commandMap.put(x.getUUID(), (DummyCommand) x));
        logger.debug("This is my command map: {}", commandMap);
    }
    private DummyCommand loadPermissionByName(String name) {
        if (!commandMap.containsKey(name)) {
            DummyCommand cmd = new DummyCommand(name, new Permission("whitelist", new ArrayList<>(), "whitelist", new ArrayList<>()));
            commandMap.put(name, cmd);
            Config.getConfigByName("permissions").putConfigElement(cmd);
            return cmd;
        } else {
            return commandMap.get(name);
        }
    }

    public void removePermissions(String commandName) {
        commandMap.remove(commandName);
        Config.getConfigByName("permissions").removeConfigElement(commandName);
    }

    public void addUser(String commandName, long userID) {
        DummyCommand command = loadPermissionByName(commandName);
        command.permission.getUsers().add(userID);
        Config.getConfigByName("permissions").putConfigElement(command);
    }

    public boolean delUser(String commandName, long userID) {
        DummyCommand command = loadPermissionByName(commandName);
        boolean existed = command.permission.getUsers().remove(userID);
        Config.getConfigByName("permissions").putConfigElement(command);
        return existed;
    }

    public void addRole(String commandName, long roleID) {
        DummyCommand command = loadPermissionByName(commandName);
        command.permission.getRoles().add(roleID);
        Config.getConfigByName("permissions").putConfigElement(command);
    }

    public boolean delRole(String commandName, long roleID) {
        DummyCommand command = loadPermissionByName(commandName);
        boolean existed = command.permission.getRoles().remove(roleID);
        Config.getConfigByName("permissions").putConfigElement(command);
        return existed;
    }

    public void setRolesMode(String commandName, boolean whitelist) {
        DummyCommand command = loadPermissionByName(commandName);
        command.permission.setRoleMode(whitelist ? "whitelist" : "blacklist");
        Config.getConfigByName("permissions").putConfigElement(command);
    }

    public void setUsersMode(String commandName, boolean whitelist) {
        DummyCommand command = loadPermissionByName(commandName);
        command.permission.setUserMode(whitelist ? "whitelist" : "blacklist");
        Config.getConfigByName("permissions").putConfigElement(command);
    }

    public boolean hasPermission(long userID, Collection<Long> roleIDs, String name) {
        return loadPermissionByName(name).permission.isAllowed(userID, roleIDs);
    }

    //Discord permissions command stuff starts here
    @DiscordCommand("permissions")
    public int permissions(IMessage msg, String...args) {
        if (args.length > 1) {
            if (commandMap.get(args[1]) == null) {
                MessageSending.sendMessage(msg.getChannel(), "`" + args[1] + "` is not a valid command name");
                return -1;
            }
        }
        return 1;
    }
    //USERS sub command block
    @DiscordSubCommand(name = "users", parent = "permissions")
    private int permissionsUsers(IMessage msg, String...args) {
        return 0;
    }

    //permissions <command> users add <user>
    @DiscordSubCommand(name = "add", parent = "permissionsUsers")
    private void permissionsUsersAdd(IMessage msg, String...args) {
        if (args.length < 5) {
            MessageSending.sendMessage(msg.getChannel(), "Please specify a user either by @mention or by ID");
            return;
        }
        long userID = MessageParsing.getUserID(args[4]);
        if (userID == -1) {
            MessageSending.sendMessage(msg.getChannel(), "`" + args[4] + "` is not a valid argument");
            return;
        }
        IUser user = msg.getClient().getUserByID(userID);
        if (user == null) {
            MessageSending.sendMessage(msg.getChannel(), "<@" + args[4] + "> is not a valid user on this server");
            return;
        }
        addUser(args[1], user.getLongID());
        MessageSending.sendMessage(msg.getChannel(), "Added " + user.getDisplayName(msg.getGuild()) + " to the " + loadPermissionByName(args[1]).permission.getUserMode() + " for command `" + args[1] + "`.");
    }
    @DiscordSubCommand(name = "remove", parent = "permissionsUsers")
    private void permissionsUsersRemove(IMessage msg, String...args) {
                if (args.length < 5) {
            MessageSending.sendMessage(msg.getChannel(), "Please specify a user either by @mention or by ID");
            return;
        }
        long userID = MessageParsing.getUserID(args[4]);
        if (userID == -1) {
            MessageSending.sendMessage(msg.getChannel(), "`" + args[4] + "` is not a valid argument");
            return;
        }

        if (delUser(args[1], userID)) {
            IUser user = msg.getClient().getUserByID(userID);
            MessageSending.sendMessage(msg.getChannel(), "Removed " + (user == null ? "The user did not exist anymore" : user.getDisplayName(msg.getGuild())) + " from the " + loadPermissionByName(args[1]).permission.getUserMode() + " for command `" + args[1] + "`.");
        } else {
            MessageSending.sendMessage(msg.getChannel(), "User " + args[4] + " was not on the " + loadPermissionByName(args[1]).permission.getUserMode() + " for command `" + args[1] + "`.");
        }


    }

    //permissions <command> users mode blacklist|whitelist
    @DiscordSubCommand(name = "mode", parent = "permissionsUsers")
    private void permissionsUsersMode(IMessage msg, String...args) {

        switch (args[4]) {
            case "blacklist":
                setUsersMode(args[1], false);
                break;
            case "whitelist":
                setUsersMode(args[1], true);
                break;
            default:
                MessageSending.sendMessage(msg.getChannel(), "`" + args[4] + "` is not a valid argument");
                return;
        }
        MessageSending.sendMessage(msg.getChannel(), "The Mode for Users is now " + args[4]);
    }

    @DiscordSubCommand(name = "list", parent = "permissionsUsers")
    private void permissionsUsersList(IMessage msg, String...args) {

        DummyCommand cmd = loadPermissionByName(args[1]);
        Permission cmdPermission = cmd.permission;
        StringBuilder builder = new StringBuilder();
        IGuild guild = msg.getGuild();

        builder.append("User ").append(cmdPermission.getUserMode()).append(" contains: ```");
        cmdPermission.getUsers().forEach(x -> {
            IUser user = guild.getUserByID(x);
            //Fixed null pointer exception, hopefully
            builder.append(user == null ? "USER IS NOT ON THIS SERVER OR DOES NOT EXIST ANYMORE" : user.getDisplayName(guild)).append(": ").append(x).append('\n');
        });
        builder.deleteCharAt(builder.length() - 1).append("```");

        MessageSending.sendMessage(msg.getChannel(), builder.toString());
    }


    //ROLES Subcommand block
    @DiscordSubCommand(name = "roles", parent = "permissions")
    private int permissionsRoles(IMessage msg, String...args) {
        return 0;
    }

    @DiscordSubCommand(name = "add", parent = "permissionsRoles")
    private void permissionsRolesAdd(IMessage msg, String...args) {
        if (args.length < 5) {
            MessageSending.sendMessage(msg.getChannel(), "Please specify a role either by @mention or by ID");
            return;
        }
        long roleID = MessageParsing.getGroupID(args[4]);
        if (roleID == -1) {
            MessageSending.sendMessage(msg.getChannel(), "`" + args[4] + "` is not a valid argument");
            return;
        }
        IRole role = msg.getClient().getRoleByID(roleID);
        if (role == null) {
            MessageSending.sendMessage(msg.getChannel(), "<@" + args[4] + "> is not a valid role on this server");
            return;
        }
        addRole(args[1], role.getLongID());
        MessageSending.sendMessage(msg.getChannel(), "Added " + role.getName() + " to the " + loadPermissionByName(args[1]).permission.getRoleMode() + " for command `" + args[1] + "`.");
    }

    /*public void addRoleToPermission(String cmdName, Long roleId) {
        Command cmd = cmdHandler.getCommandByName(cmdName);
        Permission cmdPermission = cmd.getPermission();
        cmdPermission.getRoles().add(roleId);
        Config.getConfigByName("permissions").putConfigElement(new DummyCommand(cmdName, cmdPermission));
    }*/

    @DiscordSubCommand(name = "remove", parent = "permissionsRoles")
    private void permissionsRolesRemove(IMessage msg, String...args) {
        if (args.length < 5) {
            MessageSending.sendMessage(msg.getChannel(), "Please specify a role either by @mention or by ID");
            return;
        }
        long roleID = MessageParsing.getGroupID(args[4]);
        if (roleID == -1) {
            MessageSending.sendMessage(msg.getChannel(), "`" + args[4] + "` is not a valid argument");
            return;
        }
        IRole role = msg.getClient().getRoleByID(roleID);

        if (delRole(args[1], roleID)) {
            MessageSending.sendMessage(msg.getChannel(), "Removed " + (role == null ? "ROLE DID NOT EXIST ANYMORE" : role.getName()) + " from the " + loadPermissionByName(args[1]).permission.getRoleMode() + " for command `" + args[1] + "`.");
        } else {
            MessageSending.sendMessage(msg.getChannel(), "Role " + args[4] + " was not on the " + loadPermissionByName(args[1]).permission.getRoleMode() + " for command `" + args[1] + "`.");
        }
    }

    @DiscordSubCommand(name = "mode", parent = "permissionsRoles")
    private void permissionsRolesMode(IMessage msg, String...args) {

        switch (args[4]) {
            case "blacklist":
                setRolesMode(args[1], false);
                break;
            case "whitelist":
                setRolesMode(args[1], true);
                break;
            default:
                MessageSending.sendMessage(msg.getChannel(), "`" + args[4] + "` is not a valid argument");
                return;
        }
        MessageSending.sendMessage(msg.getChannel(), "The Mode for Roles is now " + args[4]);
    }

    @DiscordSubCommand(name = "list", parent = "permissionsRoles")
    private void permissionsRolesList(IMessage msg, String...args) {
        Permission cmdPermission = loadPermissionByName(args[1]).permission;
        IGuild guild = msg.getGuild();
        StringBuilder builder = new StringBuilder();


        builder.append("Roles ").append(cmdPermission.getRoleMode()).append(" contains: ```");
        cmdPermission.getRoles().forEach(x -> {
            IRole role = guild.getRoleByID(x);
            //Fixed bug null pointer exceptions for deleted roles
            builder.append(role == null ? "DOES NOT EXIST ANYMORE" : role.getName()).append(": ").append(x).append('\n');
        });
        builder.deleteCharAt(builder.length() - 1).append("```");

        MessageSending.sendMessage(msg.getChannel(), builder.toString());
    }

    @Override
    public Collection<Command> getDiscordCommands() {
        return AnnotationExtractor.getCommandList(this);
    }

    private class DummyCommand implements ConfigElement {
        String name;
        Permission permission;
        private DummyCommand() {
        }
        DummyCommand(String name, Permission permission) {
            this.name = name;
            this.permission = permission;
        }

        @Override
        public String getUUID() {
            return name;
        }

    }
    private class Permission {
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

        public boolean isAllowed(long userID, Collection<Long> roleIDs) {

            if (users.contains(userID) && userMode.equals("blacklist")) {
                return false;
            } else if (users.contains(userID) && userMode.equals("whitelist")) {
                return true;
            }

            if (roleMode.equals("blacklist") && userMode.equals("whitelist") && roles.isEmpty() && users.isEmpty()) return false;

            if (roleIDs.stream().filter(roleID -> roles.contains(roleID)).count() > 0) {
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
}
