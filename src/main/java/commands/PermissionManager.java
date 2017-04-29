package commands;

import config.ConfigElement;
import config.ConfigObject;
import main.Utils;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

public class PermissionManager {
    CommandHandler cmdHandler;
    ConfigObject commandPermissions;
    public PermissionManager(CommandHandler cmdHandler, ConfigObject commandPermissions) {
        this.cmdHandler = cmdHandler;
        this.commandPermissions = commandPermissions;

    }

    public Permission getPermissionByName(String name) {
        //TODO: What if no permission is found in the command.json?
        DummyCommand cmd = commandPermissions.getObjectbyName(name, DummyCommand.class);
        if (cmd == null) {
            cmd = commandPermissions.getObjectbyName("ping", DummyCommand.class);
        }
        return cmd.permission;
    }

    @DiscordCommand("permissions")
    public int permissions(IMessage msg, String...args) {
        if (args.length > 1) {
            if (cmdHandler.getCommandByName(args[1]) == null) {
                msg.getChannel().sendMessage("`" + args[1] + "` is not a valid command name");
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
            msg.getChannel().sendMessage("Please specify a user either by @mention or by ID");
            return;
        }
        if (!(args[4].matches("\\d{18,19}+") || args[4].matches("<@!??\\d{18,19}?>"))) {
            msg.getChannel().sendMessage("`" + args[4] + "` is not a valid argument");
            return;
        }
        IUser user = Utils.getUser(msg.getGuild(), args[4]);
        if (user == null) {
            msg.getChannel().sendMessage("<@" + args[4] + "> is not a valid user on this server");
            return;
        }
        Command cmd = cmdHandler.getCommandByName(args[1]);
        Permission cmdPermission = cmd.getPermission();
        cmdPermission.getUsers().add(user.getLongID());
        commandPermissions.putObject(new DummyCommand(args[1], cmdPermission));
        msg.getChannel().sendMessage("Added " + user.getDisplayName(msg.getGuild()) + " to the " + cmdPermission.getUserMode() + " for command `" + args[1] + "`.");
    }

    @DiscordSubCommand(name = "remove", parent = "permissionsUsers")
    private void permissionsUsersRemove(IMessage msg, String...args) {
        if (args.length < 5) {
            msg.getChannel().sendMessage("Please specify a user either by @mention or by ID");
            return;
        }
        if (!(args[4].matches("\\d{18,19}+") || args[4].matches("<@!??\\d{18,19}?>"))) {
            msg.getChannel().sendMessage("`" + args[4] + "` is not a valid argument");
            return;
        }
        IUser user = Utils.getUser(msg.getGuild(), args[4]);
        if (user == null) {
            msg.getChannel().sendMessage("<@" + args[4] + "> is not a valid user on this server");
            return;
        }
        Command cmd = cmdHandler.getCommandByName(args[1]);
        Permission cmdPermission = cmd.getPermission();
        cmdPermission.getUsers().remove(user.getLongID());
        commandPermissions.putObject(new DummyCommand(args[1], cmdPermission));
        msg.getChannel().sendMessage("Removed " + user.getDisplayName(msg.getGuild()) + " from the " + cmdPermission.getUserMode() + " for command `" + args[1] + "`.");
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
                msg.getChannel().sendMessage("`" + args[4] + "` is not a valid argument");
                return;
        }
        commandPermissions.putObject(new DummyCommand(args[1], cmdPermission));
        msg.getChannel().sendMessage("The Mode for Users is now " + args[4]);
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

        msg.getChannel().sendMessage(builder.toString());
    }


    //ROLES Subcommand block
    @DiscordSubCommand(name = "roles", parent = "permissions")
    private int permissionsRoles(IMessage msg, String...args) {
        return 0;
    }

    @DiscordSubCommand(name = "add", parent = "permissionsRoles")
    private void permissionsRolesAdd(IMessage msg, String...args) {
        if (args.length < 5) {
            msg.getChannel().sendMessage("Please specify a role either by @mention or by ID");
            return;
        }
        if (!(args[4].matches("\\d{18,19}+") || args[4].matches("<@&\\d{18,19}?>"))) {
            msg.getChannel().sendMessage("`" + args[4] + "` is not a valid argument");
            return;
        }
        IRole role = Utils.getRole(msg.getGuild(), args[4]);
        if (role == null) {
            msg.getChannel().sendMessage("<@" + args[4] + "> is not a valid role on this server");
            return;
        }
        Command cmd = cmdHandler.getCommandByName(args[1]);
        Permission cmdPermission = cmd.getPermission();
        cmdPermission.getRoles().add(role.getLongID());
        commandPermissions.putObject(new DummyCommand(args[1], cmdPermission));
        msg.getChannel().sendMessage("Added " + role.getName() + " to the " + cmdPermission.getRoleMode() + " for command `" + args[1] + "`.");
    }

    @DiscordSubCommand(name = "remove", parent = "permissionsRoles")
    private void permissionsRolesRemove(IMessage msg, String...args) {
        if (args.length < 5) {
            msg.getChannel().sendMessage("Please specify a role either by @mention or by ID");
            return;
        }
        if (!(args[4].matches("\\d{18,19}+") || args[4].matches("<@&\\d{18,19}?>"))) {
            msg.getChannel().sendMessage("`" + args[4] + "` is not a valid argument");
            return;
        }
        IRole role = Utils.getRole(msg.getGuild(), args[4]);
        if (role == null) {
            msg.getChannel().sendMessage("<@" + args[4] + "> is not a valid role on this server");
            return;
        }
        Command cmd = cmdHandler.getCommandByName(args[1]);
        Permission cmdPermission = cmd.getPermission();
        cmdPermission.getRoles().remove(role.getLongID());
        commandPermissions.putObject(new DummyCommand(args[1], cmdPermission));
        msg.getChannel().sendMessage("Removed " + role.getName() + " from the " + cmdPermission.getRoleMode() + " for command `" + args[1] + "`.");
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
                msg.getChannel().sendMessage("`" + args[4] + "` is not a valid argument");
                return;
        }
        commandPermissions.putObject(new DummyCommand(args[1], cmdPermission));
        msg.getChannel().sendMessage("The Mode for Roles is now " + args[4]);
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

        msg.getChannel().sendMessage(builder.toString());
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

        @Override
        public String getName() {
            return name;
        }
    }


}
