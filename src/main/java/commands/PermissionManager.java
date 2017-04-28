package commands;

import config.ConfigElement;
import config.ConfigObject;
import sx.blah.discord.handle.obj.IMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionManager {
    CommandHandler cmdHandler;
    ConfigObject commandPermissions;
    //TODO Handle saving of changed permissions, need reference to commands.json
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
    public void permission(IMessage msg, String...args) {
        if (args.length < 3) {
            msg.getChannel().sendMessage("Not enough Arguments");
            return;
        }
        Permission toChange = cmdHandler.getCommandByName(args[1]).getPermission();
        if (toChange == null) {
            msg.getChannel().sendMessage("Illegal Argument: `" + args[1] + "` is not a valid command");
            return;
        }
        if (!args[2].matches("users|roles")) {
            msg.getChannel().sendMessage("Illegal Argument: `" + args[2] + "`");
            return;
        }
        if (!args[3].matches("add|remove|mode|list")) {
            msg.getChannel().sendMessage("Illegal Argument: `" + args[3] + "`");
            return;
        }
        List<Long> idList = null;
        String mode = "";
        switch (args[2]) {
            case "users":
                idList = toChange.getUsers();
                mode = toChange.getUserMode();
                break;
            case "roles":
                idList = toChange.getRoles();
                mode = toChange.getRoleMode();
                break;

        }

        //TODO Convert userIDs/roleIDs to human readable format
        switch (args[3]) {
            case "list":
                StringBuilder builder = new StringBuilder();
                builder.append(args[2]).append(' ').append(mode).append(" for command `").append(args[1]).append("` :\n");
                idList.forEach(x -> builder.append(x).append('\n'));
                msg.getChannel().sendMessage(builder.toString());
        }

    }
    private class DummyCommand implements ConfigElement {
        String name;
        Permission permission;

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
