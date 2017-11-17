package usbbot.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RequestBuffer;

import java.io.InputStream;
import java.util.concurrent.Future;

public class MessageSending {
    private static Logger logger = LoggerFactory.getLogger(MessageSending.class);

    public static Future<IMessage> sendMessage(IChannel channel, String message) {
        //INFO This just buffers the message sending for now, it could be made smarter with bundling messages to the same channel together if a RLE happens
        //TODO retry on DiscordException with text "Discord didn't return a response" and "cloudflare" in the message
        return RequestBuffer.request(() -> {
            try {
                return channel.sendMessage(message);
            } catch (DiscordException e) {
                logger.debug("I got an error trying to send a message: {}", e.getErrorMessage(), e);
                throw e;
            }
        });
    }

    public static Future<IMessage> sendFile(IChannel channel, String message, InputStream inputStream, String fileName) {
        return RequestBuffer.request(() -> {
            try {
                return channel.sendFile(message, inputStream, fileName);
            } catch (DiscordException e) {
                logger.debug("I got an error trying to send a message: {}", e.getErrorMessage(), e);
                throw e;
            }
        });
    }

    //public static Future<IMessage> sendEmbed(IChannel channel, Embed)

}
