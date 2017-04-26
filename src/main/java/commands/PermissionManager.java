package commands;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import sx.blah.discord.handle.obj.IMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionManager {
    CommandHandler cmdHandler;
    Map<String, Permission> commandPermissions = new HashMap<>();
    //TODO Handle saving of changed permissions, need reference to commands.json
    public PermissionManager(CommandHandler cmdHandler, JsonArray commands) {
        this.cmdHandler = cmdHandler;
        Gson gson = new Gson();
        for(JsonElement element : commands) {
            JsonObject jsonObject = element.getAsJsonObject();

            commandPermissions.put(jsonObject.getAsJsonPrimitive("name").getAsString(), gson.fromJson(jsonObject.getAsJsonObject("permission"), Permission.class));
        }
    }

    public Permission getPermissionByName(String name) {
        //TODO: What if no permission is found in the command.json
        return commandPermissions.get(name);
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
            case "user":
                idList = toChange.getUsers();
                mode = toChange.getUserMode();
                break;
            case "role":
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


}
