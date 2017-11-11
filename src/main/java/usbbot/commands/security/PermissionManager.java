package usbbot.commands.security;

import usbbot.commands.DiscordCommands;
import usbbot.commands.core.Command;
import usbbot.config.CommandPermission;
import usbbot.config.MiscSQLCommand;
import usbbot.util.commands.AnnotationExtractor;
import usbbot.util.commands.DiscordCommand;
import usbbot.util.commands.DiscordSubCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import usbbot.util.MessageParsing;
import usbbot.util.MessageSending;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

import java.util.*;

public class PermissionManager implements DiscordCommands {
    private static Logger logger = LoggerFactory.getLogger(PermissionManager.class);


    public static boolean hasPermission(long guildID, long userID, Collection<Long> roleIDs, String name) {
        CommandPermission permission = new CommandPermission(guildID, name);
        if (permission.isUserModeBlacklist()) {
            if (permission.containsUser(userID)) {
                return false;
            }
        } else {
            if(permission.containsUser(userID)) {
                return true;
            }
            if (permission.isRoleModeBlacklist() && !permission.anyListPopulated()) {
                return false;
            }
        }

        if (permission.isRoleModeBlacklist()) {
            if (!permission.containsAnyRole(roleIDs)) {
                return true;
            }
        } else {
            if (permission.containsAnyRole(roleIDs)) {
                return true;
            }
        }
        return false;
    }

    //Discord permissions command stuff starts here
    @DiscordCommand("permissions")
    public int permissions(IMessage msg, String...args) {
        if (args.length > 1) {
            if (!MiscSQLCommand.commandExists(msg.getGuild().getLongID(), args[1])) {
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
        CommandPermission permission = new CommandPermission(msg.getGuild().getLongID(), args[1]);
        permission.addUser(user.getLongID());
        MessageSending.sendMessage(msg.getChannel(), "Added " + user.getDisplayName(msg.getGuild()) + " to the " + (permission.isUserModeBlacklist() ? "blacklist" : "whitelist") + " for command `" + args[1] + "`.");
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
		CommandPermission permission = new CommandPermission(msg.getGuild().getLongID(), args[1]);

        if (permission.delUser(userID)) {
            IUser user = msg.getClient().getUserByID(userID);
            MessageSending.sendMessage(msg.getChannel(), "Removed " + (user == null ? "The user did not exist anymore" : user.getDisplayName(msg.getGuild())) + " from the " + (permission.isUserModeBlacklist() ? "blacklist" : "whitelist") + " for command `" + args[1] + "`.");
        } else {
            MessageSending.sendMessage(msg.getChannel(), "User " + args[4] + " was not on the " + (permission.isUserModeBlacklist() ? "blacklist" : "whitelist") + " for command `" + args[1] + "`.");
        }


    }

    //permissions <command> users mode blacklist|whitelist
    @DiscordSubCommand(name = "mode", parent = "permissionsUsers")
    private void permissionsUsersMode(IMessage msg, String...args) {
    	CommandPermission permission = new CommandPermission(msg.getGuild().getLongID(), args[1]);
    	if (args.length <= 4) {
    		MessageSending.sendMessage(msg.getChannel(), "Specify either blacklist or whitelist");
    		return;
		}
		switch (args[4]) {
			case "blacklist":
				permission.setUserMode(true);
				break;
			case "whitelist":
				permission.setUserMode(false);
				break;
			default:
				MessageSending.sendMessage(msg.getChannel(), "`" + args[4] + "` is not a valid argument");
				return;
		}
		MessageSending.sendMessage(msg.getChannel(), "The Mode for Users is now " + args[4]);
    }

    @DiscordSubCommand(name = "list", parent = "permissionsUsers")
    private void permissionsUsersList(IMessage msg, String...args) {
		IGuild guild = msg.getGuild();
		CommandPermission permission = new CommandPermission(guild.getLongID(), args[1]);
		logger.debug("Looking up all users for command {} on guild {}", args[1], guild.getLongID());
        StringBuilder builder = new StringBuilder();


        builder.append("User ").append(permission.isUserModeBlacklist() ? "blacklist" : "whitelist").append(" contains: ```");
        permission.getAllUserIDs().forEach(x -> {
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
        long roleID;
        if (args[4].equals("@everyone")) {
            roleID = msg.getGuild().getEveryoneRole().getLongID();
        } else {
            roleID = MessageParsing.getGroupID(args[4]);
        }
        if (roleID == -1) {
            MessageSending.sendMessage(msg.getChannel(), "`" + args[4] + "` is not a valid argument");
            return;
        }
        IRole role = msg.getClient().getRoleByID(roleID);
        if (role == null) {
            MessageSending.sendMessage(msg.getChannel(), "<@" + args[4] + "> is not a valid role on this server");
            return;
        }
        CommandPermission permission = new CommandPermission(msg.getGuild().getLongID(), args[1]);
        permission.addRole(role.getLongID());
        MessageSending.sendMessage(msg.getChannel(), "Added " + role.getName() + " to the " + (permission.isRoleModeBlacklist() ? "blacklist" : "whitelist") + " for command `" + args[1] + "`.");
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
        long roleID;
        if (args[4].equals("@everyone")) {
            roleID = msg.getGuild().getEveryoneRole().getLongID();
        } else {
            roleID = MessageParsing.getGroupID(args[4]);
        }
        if (roleID == -1) {
            MessageSending.sendMessage(msg.getChannel(), "`" + args[4] + "` is not a valid argument");
            return;
        }
        IRole role = msg.getClient().getRoleByID(roleID);
		CommandPermission permission = new CommandPermission(msg.getGuild().getLongID(), args[1]);
        if (permission.delRole(roleID)) {
            MessageSending.sendMessage(msg.getChannel(), "Removed " + (role == null ? "ROLE DID NOT EXIST ANYMORE" : role.getName()) + " from the " + (permission.isRoleModeBlacklist() ? "blacklist" : "whitelist") + " for command `" + args[1] + "`.");
        } else {
            MessageSending.sendMessage(msg.getChannel(), "Role " + args[4] + " was not on the " + (permission.isRoleModeBlacklist() ? "blacklist" : "whitelist") + " for command `" + args[1] + "`.");
        }
    }

    @DiscordSubCommand(name = "mode", parent = "permissionsRoles")
    private void permissionsRolesMode(IMessage msg, String...args) {
		CommandPermission permission = new CommandPermission(msg.getGuild().getLongID(), args[1]);
		if (args.length <= 4) {
			MessageSending.sendMessage(msg.getChannel(), "Specify either blacklist or whitelist");
			return;
		}
        switch (args[4]) {
            case "blacklist":
            	permission.setRoleMode(true);
                break;
            case "whitelist":
            	permission.setRoleMode(false);
                break;
            default:
                MessageSending.sendMessage(msg.getChannel(), "`" + args[4] + "` is not a valid argument");
                return;
        }
        MessageSending.sendMessage(msg.getChannel(), "The Mode for Roles is now " + args[4]);
    }

    @DiscordSubCommand(name = "list", parent = "permissionsRoles")
    private void permissionsRolesList(IMessage msg, String...args) {
    	CommandPermission permission = new CommandPermission(msg.getGuild().getLongID(), args[1]);
        IGuild guild = msg.getGuild();
        StringBuilder builder = new StringBuilder();


        builder.append("Roles ").append(permission.isRoleModeBlacklist() ? "blacklist" : "whitelist").append(" contains: ```");
        permission.getAllRoleIDs().forEach(x -> {
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
}