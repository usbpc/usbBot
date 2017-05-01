package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RequestBuffer;

import java.io.InputStream;

public class MessageSending {
    private static Logger logger = LoggerFactory.getLogger(MessageSending.class);
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
        //TODO deal smarter with exceptions and bundle messages to the same channel if RLE happens
        RequestBuffer.request(() -> {
            try {
                channel.sendMessage(message);
            } catch (DiscordException e) {
                logger.debug("I got an error trying to send a message: {}", e.getErrorMessage(), e);
                //System.out.printf("[MessageSending] I got an error trying to send a message: %s \r\n This is the stacktrace %s", e.getErrorMessage(), Arrays.toString(e.getStackTrace()));
                throw e;
            }
        });
    }

    public static void sendFile(IChannel channel, String message, InputStream inputStream, String fileName) {
        channel.sendFile(message, inputStream, fileName);
    }
}
