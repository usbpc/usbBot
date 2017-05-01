package commands.security;

import commands.core.Command;
import commands.core.CommandHandler;
import commands.annotations.DiscordCommand;
import commands.annotations.DiscordSubCommand;
import config.Config;
import config.ConfigElement;
import util.MessageSending;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

import java.util.ArrayList;

public class PermissionManager {
    private CommandHandler cmdHandler;
    public PermissionManager(CommandHandler cmdHandler) {
        this.cmdHandler = cmdHandler;

    }

    public Permission loadPermissionByName(String name) {
        DummyCommand cmd = Config.getConfigByName("permissions").getObjectByName(name, DummyCommand.class);
        if (cmd == null) {
            cmd = new DummyCommand(name, new Permission("whitelist", new ArrayList<>(), "whitelist", new ArrayList<>()));
            Config.getConfigByName("permissions").putConfigElement(cmd);
        }
        return cmd.permission;
    }


    //Discord permissions command stuff starts here
    @DiscordCommand("permissions")
    public int permissions(IMessage msg, String...args) {
        if (args.length > 1) {
            if (cmdHandler.getCommandByName(args[1]) == null) {
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
        if (!(args[4].matches("\\d{18,19}+") || args[4].matches("<@!??\\d{18,19}?>"))) {
            MessageSending.sendMessage(msg.getChannel(), "`" + args[4] + "` is not a valid argument");
            return;
        }
        IUser user = MessageSending.getUser(msg.getGuild(), args[4]);
        if (user == null) {
            MessageSending.sendMessage(msg.getChannel(), "<@" + args[4] + "> is not a valid user on this server");
            return;
        }
        Command cmd = cmdHandler.getCommandByName(args[1]);
        Permission cmdPermission = cmd.getPermission();
        cmdPermission.getUsers().add(user.getLongID());
        Config.getConfigByName("permissions").putConfigElement(new DummyCommand(args[1], cmdPermission));
        MessageSending.sendMessage(msg.getChannel(), "Added " + user.getDisplayName(msg.getGuild()) + " to the " + cmdPermission.getUserMode() + " for command `" + args[1] + "`.");
    }

    @DiscordSubCommand(name = "remove", parent = "permissionsUsers")
    private void permissionsUsersRemove(IMessage msg, String...args) {
        if (args.length < 5) {
            MessageSending.sendMessage(msg.getChannel(), "Please specify a user either by @mention or by ID");
            return;
        }
        if (!(args[4].matches("\\d{18,19}+") || args[4].matches("<@!??\\d{18,19}?>"))) {
            MessageSending.sendMessage(msg.getChannel(), "`" + args[4] + "` is not a valid argument");
            return;
        }
        IUser user = MessageSending.getUser(msg.getGuild(), args[4]);
        if (user == null) {
            MessageSending.sendMessage(msg.getChannel(), "<@" + args[4] + "> is not a valid user on this server");
            return;
        }
        Command cmd = cmdHandler.getCommandByName(args[1]);
        Permission cmdPermission = cmd.getPermission();
        cmdPermission.getUsers().remove(user.getLongID());
        Config.getConfigByName("permissions").putConfigElement(new DummyCommand(args[1], cmdPermission));
        MessageSending.sendMessage(msg.getChannel(), "Removed " + user.getDisplayName(msg.getGuild()) + " from the " + cmdPermission.getUserMode() + " for command `" + args[1] + "`.");
    }

    //permissions <command> users mode blacklist|whitelist
    @DiscordSubCommand(name = "mode", parent = "permissionsUsers")
    private void permissionsUsersMode(IMessage msg, String...args) {
        Command cmd = cmdHandler.getCommandByName(args[1]);
        Permission cmdPermission = cmd.getPermission();

        switch (args[4]) {
            case "blacklist":
                cmdPermission.setUserMode("blacklist");
                break;
            case "whitelist":
                cmdPermission.setUserMode("whitelist");
                break;
            default:
                MessageSending.sendMessage(msg.getChannel(), "`" + args[4] + "` is not a valid argument");
                return;
        }
        Config.getConfigByName("permissions").putConfigElement(new DummyCommand(args[1], cmdPermission));
        MessageSending.sendMessage(msg.getChannel(), "The Mode for Users is now " + args[4]);
    }

    @DiscordSubCommand(name = "list", parent = "permissionsUsers")
    private void permissionsUsersList(IMessage msg, String...args) {
        Command cmd = cmdHandler.getCommandByName(args[1]);
        Permission cmdPermission = cmd.getPermission();
        StringBuilder builder = new StringBuilder();
        IGuild guild = msg.getGuild();

        builder.append("User ").append(cmdPermission.getUserMode()).append(" contains: ```");
        cmdPermission.getUsers().forEach(x -> {
            IUser user = guild.getUserByID(x);
            builder.append(user.getName()).append(": ").append(user.getLongID()).append('\n');
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
        if (!(args[4].matches("\\d{18,19}+") || args[4].matches("<@&\\d{18,19}?>"))) {
            MessageSending.sendMessage(msg.getChannel(), "`" + args[4] + "` is not a valid argument");
            return;
        }
        IRole role = MessageSending.getRole(msg.getGuild(), args[4]);
        if (role == null) {
            MessageSending.sendMessage(msg.getChannel(), "<@" + args[4] + "> is not a valid role on this server");
            return;
        }
        Command cmd = cmdHandler.getCommandByName(args[1]);
        Permission cmdPermission = cmd.getPermission();
        cmdPermission.getRoles().add(role.getLongID());
        Config.getConfigByName("permissions").putConfigElement(new DummyCommand(args[1], cmdPermission));
        MessageSending.sendMessage(msg.getChannel(), "Added " + role.getName() + " to the " + cmdPermission.getRoleMode() + " for command `" + args[1] + "`.");
    }

    public void addRoleToPermission(String cmdName, Long roleId) {
        Command cmd = cmdHandler.getCommandByName(cmdName);
        Permission cmdPermission = cmd.getPermission();
        cmdPermission.getRoles().add(roleId);
        Config.getConfigByName("permissions").putConfigElement(new DummyCommand(cmdName, cmdPermission));
    }

    @DiscordSubCommand(name = "remove", parent = "permissionsRoles")
    private void permissionsRolesRemove(IMessage msg, String...args) {
        if (args.length < 5) {
            MessageSending.sendMessage(msg.getChannel(), "Please specify a role either by @mention or by ID");
            return;
        }
        if (!(args[4].matches("\\d{18,19}+") || args[4].matches("<@&\\d{18,19}?>"))) {
            MessageSending.sendMessage(msg.getChannel(), "`" + args[4] + "` is not a valid argument");
            return;
        }
        IRole role = MessageSending.getRole(msg.getGuild(), args[4]);
        if (role == null) {
            MessageSending.sendMessage(msg.getChannel(), "<@" + args[4] + "> is not a valid role on this server");
            return;
        }
        Command cmd = cmdHandler.getCommandByName(args[1]);
        Permission cmdPermission = cmd.getPermission();
        cmdPermission.getRoles().remove(role.getLongID());
        Config.getConfigByName("permissions").putConfigElement(new DummyCommand(args[1], cmdPermission));
        MessageSending.sendMessage(msg.getChannel(), "Removed " + role.getName() + " from the " + cmdPermission.getRoleMode() + " for command `" + args[1] + "`.");
    }

    @DiscordSubCommand(name = "mode", parent = "permissionsRoles")
    private void permissionsRolesMode(IMessage msg, String...args) {
        Command cmd = cmdHandler.getCommandByName(args[1]);
        Permission cmdPermission = cmd.getPermission();

        switch (args[4]) {
            case "blacklist":
                cmdPermission.setRoleMode("blacklist");
                break;
            case "whitelist":
                cmdPermission.setRoleMode("whitelist");
                break;
            default:
                MessageSending.sendMessage(msg.getChannel(), "`" + args[4] + "` is not a valid argument");
                return;
        }
        Config.getConfigByName("permissions").putConfigElement(new DummyCommand(args[1], cmdPermission));
        MessageSending.sendMessage(msg.getChannel(), "The Mode for Roles is now " + args[4]);
    }

    @DiscordSubCommand(name = "list", parent = "permissionsRoles")
    private void permissionsRolesList(IMessage msg, String...args) {
        Command cmd = cmdHandler.getCommandByName(args[1]);
        Permission cmdPermission = cmd.getPermission();
        StringBuilder builder = new StringBuilder();
        IGuild guild = msg.getGuild();

        builder.append("Roles ").append(cmdPermission.getRoleMode()).append(" contains: ```");
        cmdPermission.getRoles().forEach(x -> {
            IRole role = guild.getRoleByID(x);
            builder.append(role.getName()).append(": ").append(role.getLongID()).append('\n');
        });
        builder.deleteCharAt(builder.length() - 1).append("```");

        MessageSending.sendMessage(msg.getChannel(), builder.toString());
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
}
