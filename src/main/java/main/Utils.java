package main;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

public class Utils {
    public static IUser getUser(IGuild guild, String id) {
        Long userID;
        if (id.matches("\\d{18,19}+")) {
            userID = Long.valueOf(id);
        } else if (id.matches("<@!??\\d{18,19}?>")) {
            userID = Long.valueOf(id.substring((id.indexOf('!') != -1 ? id.indexOf('!') : id.indexOf('@')) + 1, id.indexOf('>')));
        } else {
            return null;
        }
        return guild.getUserByID(userID);
    }

    public static IRole getRole(IGuild guild, String id) {
        Long longId;
        if (id.matches("\\d{18,19}+")) {
            longId = Long.valueOf(id);
        } else if (id.matches("<@&\\d{18,19}?>"))  {
            longId = Long.valueOf(id.substring(3, id.length() - 1));
        } else {
            return null;
        }
        return guild.getRoleByID(longId);
    }

    public static void sendMessage(IChannel channel, String message) {
        channel.sendMessage(message);
    }
}
