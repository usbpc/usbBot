package usbbot.modules;

import sx.blah.discord.util.EmbedBuilder;
import usbbot.commands.CommandHandler;
import usbbot.commands.DiscordCommands;
import usbbot.commands.core.Command;
import usbbot.config.DBTextCommand;
import usbbot.config.DBTextCommandsKt;
import usbbot.util.commands.AnnotationExtractor;
import usbbot.util.commands.DiscordCommand;
import usbbot.util.commands.DiscordSubCommand;
import usbbot.util.MessageSending;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.handle.obj.IMessage;
import util.IMessageExtensionsKt;

import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by usbpc on 30.04.2017.
 */
public class SimpleTextResponses implements DiscordCommands {

    private Logger logger = LoggerFactory.getLogger(SimpleTextResponses.class);
    private SimpleTextCommand simpleTextCommand = new SimpleTextCommand();
    private CommandHandler commandModule;
    public SimpleTextResponses(CommandHandler commandModule) {
        this.commandModule = commandModule;
    }

    @DiscordCommand("commands")
    private int commands(IMessage msg, String...args) {
        if (args.length < 3) {
            IMessageExtensionsKt.sendError(msg.getChannel(), "Not enough arguments!");
            return -1;
        }
        return 0;
    }
    private boolean isValidImage(String maybeUrl) {
        try {
            URI url = new URI(maybeUrl);
            String scheme = url.getScheme();
            if (scheme == null) return false;
            if (!scheme.matches("https?")) return false;
            logger.trace("Host is {}", url.getHost());
            if (url.getHost() == null) return false;
            logger.trace("Path is {}", url.getPath());
            if (url.getPath().isEmpty()) return false;
            return true;
        } catch (URISyntaxException ex) {
            return false;
        }

    }
    //usbbot.commands add <commandName> <Message>
    @DiscordSubCommand(parent = "commands", name = "add")
    private void commandsAdd(IMessage msg, String...args) {
        if (args.length < 4) {
            IMessageExtensionsKt.sendError(msg.getChannel(), "Not enough arguments!");
            return;
        }
        if (commandModule.discordCommandExists(args[2], msg.getGuild().getLongID())) {
            IMessageExtensionsKt.sendError(msg.getChannel(), "`" + args[2] + "` is already a command!");
            return;
        }


        String message = msg.getContent().substring(msg.getContent().indexOf(args[2]) + args[2].length() + 1);
        Matcher matcher = imagePattern.matcher(message);
        if (matcher.find() && !isValidImage(matcher.group(1))) {
            IMessageExtensionsKt.sendError(msg.getChannel(), "You need to specify a valid url with the use of ${image:}...");
            return;
        }


        DBTextCommand cmd = DBTextCommandsKt.createDBTextCommand(msg.getGuild().getLongID(),
                args[2],
                "whitelist",
                "whitelist",
                message);
        cmd.addRoleToList(msg.getGuild().getEveryoneRole().getLongID());
        IMessageExtensionsKt.sendSuccess(msg.getChannel(), "Command `" + args[2] + "` successfully added!");
    }

    //usbbot.commands remove <commandName>
    @DiscordSubCommand(parent = "commands", name = "remove")
    private void commandsRemove(IMessage msg, String...args) {
        if (args.length < 3) {
            IMessageExtensionsKt.sendError(msg.getChannel(), "Not enough arguments.");
        } else {
            DBTextCommand cmd = DBTextCommandsKt.getDBTextCommand(msg.getGuild().getLongID(), args[2]);
            if (cmd != null) {
                cmd.delete();
                IMessageExtensionsKt.sendSuccess(msg.getChannel(), "`" + args[2] + "` successfully removed.");
            } else {
                IMessageExtensionsKt.sendError(msg.getChannel(), "`" + args[2] + "` is not a command");
            }
        }
    }

    @DiscordSubCommand(parent = "commands", name = "edit")
    private void commandsEdit(IMessage msg, String...args) {
        if (args.length < 4) {
            IMessageExtensionsKt.sendError(msg.getChannel(), "Not enough arguments.");
        } else {
            DBTextCommand cmd = DBTextCommandsKt.getDBTextCommand(msg.getGuild().getLongID(), args[2]);
            if (cmd != null) {
                String message = msg.getContent().substring(msg.getContent().indexOf(args[2]) + args[2].length() + 1);
                Matcher matcher = imagePattern.matcher(message);
                if (matcher.find() && !isValidImage(matcher.group(1))) {
                    IMessageExtensionsKt.sendError(msg.getChannel(), "You need to specify a valid url with the use of ${image:}...");
                    return;
                }
                cmd.editText(message);
                IMessageExtensionsKt.sendSuccess(msg.getChannel(), "Changed `" + args[2] + "`!");
            } else {
                IMessageExtensionsKt.sendError(msg.getChannel(), "`" + args[2] + "` is not a command");
            }
        }
    }

    @Override
    public Collection<Command> getDiscordCommands() {
        return AnnotationExtractor.getCommandList(this);
    }


    private class SimpleTextCommand extends Command {
        @Override
        public void execute(IMessage msg, String...args) {
            answer(msg, args);
        }
    }
    public static void answer(IMessage msg, String args[]) {
        DBTextCommand cmd = DBTextCommandsKt.getDBTextCommand(msg.getGuild().getLongID(), args[0]);
        answer(msg, cmd);
    }

    private static Pattern imagePattern = Pattern.compile("\\$\\{image:(.+?)\\}");

    public static void answer(IMessage msg, DBTextCommand cmd) {
        String content = cmd.getText();
        //TODO placeholder for args to be replaced by the arguments given when called
        /*
        * possible placeholders: author, args
        *
        *
        *
        * */
        EmbedBuilder embed = new EmbedBuilder().withColor(Color.GREEN);
        content = content.replaceAll("\\$\\{author\\}", msg.getAuthor().mention(true));
        Matcher matcher = imagePattern.matcher(content);
        if (matcher.find()) {
            embed.withImage(matcher.group(1));
            content = matcher.replaceAll("");
        }
        embed.withDescription(content);
        IMessageExtensionsKt.bufferedSend(msg.getChannel(), embed.build());
    }


}
