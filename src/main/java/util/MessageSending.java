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
