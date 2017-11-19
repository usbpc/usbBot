package usbbot.commands.security;

import usbbot.commands.DiscordCommands;
import usbbot.commands.core.Command;
import usbbot.config.DBCommand;
import usbbot.config.DBCommandKt;
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
import util.IMessageExtensionsKt;

import java.util.*;

public class PermissionManager implements DiscordCommands {
    private static Logger logger = LoggerFactory.getLogger(PermissionManager.class);


    public static boolean hasPermission(long guildID, long userID, Collection<Long> roleIDs, String name) {
        DBCommand cmd = DBCommandKt.getCommandForGuild(guildID, name);
        if (cmd != null) {
            Collection<Long> users = cmd.getPermissionUsers();
            Collection<Long> roles = cmd.getPermissionRoles();

            if (cmd.getUsermode().equals("blacklist")) {
                if (users.contains(userID)) {
                    return false;
                }
            } else {
                if(users.contains(userID)) {
                    return true;
                }
                if (cmd.getRolemode().equals("blacklist") && users.isEmpty() && roles.isEmpty()) {
                    return false;
                }
            }

            if (cmd.getRolemode().equals("blacklist")) {
                if (roleIDs.stream().noneMatch(roles::contains)) {
                    return true;
                }
            } else {
                if (roleIDs.stream().anyMatch(roles::contains)) {
                    return true;
                }
            }
            return false;
        } else {
            throw new IllegalStateException("There should never be a request to this methode if the command is not in the DB");
        }

    }

    //Discord permissions command stuff starts here
    @DiscordCommand("permissions")
    public int permissions(IMessage msg, String...args) {
        if (args.length > 1) {

            if (DBCommandKt.getCommandForGuild(msg.getGuild().getLongID(), args[1]) == null) {
                IMessageExtensionsKt.sendError(msg.getChannel(), "`" + args[1] + "` is not a valid command name");
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
            IMessageExtensionsKt.sendError(msg.getChannel(), "Please specify a user either by @mention or by ID");
            return;
        }
        long userID = MessageParsing.getUserID(args[4]);
        if (userID == -1) {
            IMessageExtensionsKt.sendError(msg.getChannel(), "`" + args[4] + "` is not a valid argument");
            return;
        }
        IUser user = msg.getClient().getUserByID(userID);
        if (user == null) {
            IMessageExtensionsKt.sendError(msg.getChannel(), "<@" + args[4] + "> is not a valid user on this server");
            return;
        }

        DBCommand cmd = DBCommandKt.getCommandForGuild(msg.getGuild().getLongID(), args[1]);
        if (cmd == null)
            throw new IllegalStateException("The command " + args[1] + " for Guild " + msg.getGuild().getName() + " dosen't exist!");

        cmd.addUserToList(user.getLongID());
        IMessageExtensionsKt.sendSuccess(msg.getChannel(), "Added " + user.getDisplayName(msg.getGuild()) +
                " to the " + cmd.getUsermode() + " for command `" + args[1] + "`.");
    }
    @DiscordSubCommand(name = "remove", parent = "permissionsUsers")
    private void permissionsUsersRemove(IMessage msg, String...args) {
    	if (args.length < 5) {
            IMessageExtensionsKt.sendError(msg.getChannel(), "Please specify a user either by @mention or by ID");
            return;
        }
        long userID = MessageParsing.getUserID(args[4]);
        if (userID == -1) {
            IMessageExtensionsKt.sendError(msg.getChannel(), "`" + args[4] + "` is not a valid argument");
            return;
        }
        DBCommand cmd = DBCommandKt.getCommandForGuild(msg.getGuild().getLongID(), args[1]);
        if (cmd == null)
            throw new IllegalStateException("The command " + args[1] + " for Guild " + msg.getGuild().getName() +
                    " dosen't exist!");

        if (cmd.delUserFromList(userID) >= 1) {
            IUser user = msg.getClient().getUserByID(userID);
            IMessageExtensionsKt.sendSuccess(msg.getChannel(), "Removed " +
                    (user == null ? "The user did not exist anymore" : user.getDisplayName(msg.getGuild())) +
                    " from the " + cmd.getUsermode() +
                    " for command `" + args[1] + "`.");
        } else {
            IMessageExtensionsKt.sendError(msg.getChannel(), "User " + args[4] + " was not on the " +
                    cmd.getUsermode() + " for command `" + args[1] + "`.");
        }
    }

    //permissions <command> users mode blacklist|whitelist
    @DiscordSubCommand(name = "mode", parent = "permissionsUsers")
    private void permissionsUsersMode(IMessage msg, String...args) {
        if (args.length <= 4) {
    		IMessageExtensionsKt.sendError(msg.getChannel(), "Specify either blacklist or whitelist");
    		return;
		}
        DBCommand cmd = DBCommandKt.getCommandForGuild(msg.getGuild().getLongID(), args[1]);
        if (cmd == null)
            throw new IllegalStateException("The command " + args[1] + " for Guild " + msg.getGuild().getName() + " dosen't exist!");

        switch (args[4]) {
			case "blacklist":
			case "whitelist":
			    cmd.setUserMode(args[4]);
				break;
			default:
				IMessageExtensionsKt.sendError(msg.getChannel(), "`" + args[4] + "` is not a valid argument");
				return;
		}
        IMessageExtensionsKt.sendSuccess(msg.getChannel(), "The Mode for Users is now " + args[4]);
    }

    @DiscordSubCommand(name = "list", parent = "permissionsUsers")
    private void permissionsUsersList(IMessage msg, String...args) {
		IGuild guild = msg.getGuild();
		logger.debug("Looking up all users for command {} on guild {}", args[1], guild.getLongID());
        DBCommand cmd = DBCommandKt.getCommandForGuild(msg.getGuild().getLongID(), args[1]);
        if (cmd == null)
            throw new IllegalStateException("The command " + args[1] + " for Guild " + msg.getGuild().getName() + " dosen't exist!");

        StringBuilder builder = new StringBuilder();


        builder.append("User ").append(cmd.getUsermode()).append(" contains: ```");
        cmd.getPermissionUsers().forEach(x -> {
            IUser user = guild.getUserByID(x);
            //Fixed null pointer exception, hopefully
            builder.append(user == null ? "USER IS NOT ON THIS SERVER OR DOES NOT EXIST ANYMORE" : user.getDisplayName(guild)).append(": ").append(x).append('\n');
        });
        builder.deleteCharAt(builder.length() - 1).append("```");

        IMessageExtensionsKt.sendSuccess(msg.getChannel(), builder.toString());
    }


    //ROLES Subcommand block
    @DiscordSubCommand(name = "roles", parent = "permissions")
    private int permissionsRoles(IMessage msg, String...args) {
        return 0;
    }

    @DiscordSubCommand(name = "add", parent = "permissionsRoles")
    private void permissionsRolesAdd(IMessage msg, String...args) {
        if (args.length < 5) {
            IMessageExtensionsKt.sendError(msg.getChannel(), "Please specify a role either by @mention or by ID");
            return;
        }
        long roleID;
        if (args[4].equals("@everyone")) {
            roleID = msg.getGuild().getEveryoneRole().getLongID();
        } else {
            roleID = MessageParsing.getGroupID(args[4]);
        }
        if (roleID == -1) {
            IMessageExtensionsKt.sendError(msg.getChannel(), "`" + args[4] + "` is not a valid argument");
            return;
        }
        IRole role = msg.getClient().getRoleByID(roleID);
        if (role == null) {
            IMessageExtensionsKt.sendError(msg.getChannel(), "<@" + args[4] + "> is not a valid role on this server");
            return;
        }
        DBCommand cmd = DBCommandKt.getCommandForGuild(msg.getGuild().getLongID(), args[1]);
        if (cmd == null)
            throw new IllegalStateException("The command " + args[1] + " for Guild " + msg.getGuild().getName() + " dosen't exist!");

        cmd.addRoleToList(role.getLongID());
        IMessageExtensionsKt.sendSuccess(msg.getChannel(), "Added " + role.getName() + " to the " +
                cmd.getRolemode() + " for command `" + args[1] + "`.");
    }

    /*public void addRoleToPermission(String cmdName, Long roleId) {
        DBCommand cmd = cmdHandler.getCommandByName(cmdName);
        Permission cmdPermission = cmd.getPermission();
        cmdPermission.getRoles().add(roleId);
        Config.getConfigByName("permissions").putConfigElement(new DummyCommand(cmdName, cmdPermission));
    }*/

    @DiscordSubCommand(name = "remove", parent = "permissionsRoles")
    private void permissionsRolesRemove(IMessage msg, String...args) {
        if (args.length < 5) {
            IMessageExtensionsKt.sendError(msg.getChannel(), "Please specify a role either by @mention or by ID");
            return;
        }
        long roleID;
        if (args[4].equals("@everyone")) {
            roleID = msg.getGuild().getEveryoneRole().getLongID();
        } else {
            roleID = MessageParsing.getGroupID(args[4]);
        }
        if (roleID == -1) {
            IMessageExtensionsKt.sendError(msg.getChannel(), "`" + args[4] + "` is not a valid argument");
            return;
        }
        IRole role = msg.getClient().getRoleByID(roleID);
        DBCommand cmd = DBCommandKt.getCommandForGuild(msg.getGuild().getLongID(), args[1]);
        if (cmd == null)
            throw new IllegalStateException("The command " + args[1] + " for Guild " + msg.getGuild().getName() + " dosen't exist!");

        if (cmd.delRoleFromList(roleID) >= 1) {
            IMessageExtensionsKt.sendSuccess(msg.getChannel(), "Removed " +
                    (role == null ? "ROLE DID NOT EXIST ANYMORE" : role.getName()) +
                    " from the " + cmd.getRolemode() + " for command `" + args[1] + "`.");
        } else {
            IMessageExtensionsKt.sendError(msg.getChannel(), "Role " + args[4] + " was not on the " +
                    cmd.getRolemode() + " for command `" + args[1] + "`.");
        }
    }

    @DiscordSubCommand(name = "mode", parent = "permissionsRoles")
    private void permissionsRolesMode(IMessage msg, String...args) {
		if (args.length <= 4) {
		    IMessageExtensionsKt.sendError(msg.getChannel(), "Specify either blacklist or whitelist!");
			return;
		}
        DBCommand cmd = DBCommandKt.getCommandForGuild(msg.getGuild().getLongID(), args[1]);
        if (cmd == null)
            throw new IllegalStateException("The command " + args[1] + " for Guild " + msg.getGuild().getName() + " dosen't exist!");

        switch (args[4]) {
            case "blacklist":
            case "whitelist":
            	cmd.setRoleMode(args[4]);
                break;
            default:
                IMessageExtensionsKt.sendError(msg.getChannel(), "`" + args[4] + "` is not a valid argument");
                return;
        }
        IMessageExtensionsKt.sendSuccess(msg.getChannel(), "The Mode for Roles is now " + args[4]);
    }

    @DiscordSubCommand(name = "list", parent = "permissionsRoles")
    private void permissionsRolesList(IMessage msg, String...args) {
        IGuild guild = msg.getGuild();
        StringBuilder builder = new StringBuilder();

        DBCommand cmd = DBCommandKt.getCommandForGuild(msg.getGuild().getLongID(), args[1]);
        if (cmd == null)
            throw new IllegalStateException("The command " + args[1] + " for Guild " + msg.getGuild().getName() + " dosen't exist!");


        builder.append("Roles ").append(cmd.getRolemode()).append(" contains: ```");
        cmd.getPermissionRoles().forEach(x -> {
            IRole role = guild.getRoleByID(x);
            //Fixed bug null pointer exceptions for deleted roles
            builder.append(role == null ? "DOES NOT EXIST ANYMORE" : role.getName()).append(": ").append(x).append('\n');
        });
        builder.deleteCharAt(builder.length() - 1).append("```");

        IMessageExtensionsKt.sendSuccess(msg.getChannel(), builder.toString());
    }

    @Override
    public Collection<Command> getDiscordCommands() {
        return AnnotationExtractor.getCommandList(this);
    }
}