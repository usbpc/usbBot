package modules;

import commands.CommandHandler;
import commands.DiscordCommands;
import commands.core.Command;
import config.CommandPermission;
import config.SimpleTextCommandsSQL;
import util.commands.AnnotationExtractor;
import util.commands.DiscordCommand;
import util.commands.DiscordSubCommand;
import util.MessageSending;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.handle.obj.IMessage;

import java.util.*;
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
            MessageSending.sendMessage(msg.getChannel(), "Not enough arguments!");
            return -1;
        }
        return 0;
    }

    //commands add <commandName> <Message>
    @DiscordSubCommand(parent = "commands", name = "add")
    private void commandsAdd(IMessage msg, String...args) {
        if (args.length < 4) {
            MessageSending.sendMessage(msg.getChannel(), "Not enough arguments!");
            return;
        }
        if (commandModule.discordCommandExists(args[2], msg.getGuild().getLongID())) {
            MessageSending.sendMessage(msg.getChannel(), "`" + args[2] + "` is already a command!");
            return;
        }


        String message = msg.getContent().substring(msg.getContent().indexOf(args[2]) + args[2].length() + 1);

        SimpleTextCommandsSQL.insertCommand(msg.getGuild().getLongID(), args[2], msg.getContent().substring(msg.getContent().indexOf(args[2]) + args[2].length() + 1));
        new CommandPermission(msg.getGuild().getLongID(), args[2]).addRole(msg.getGuild().getEveryoneRole().getLongID());
        MessageSending.sendMessage(msg.getChannel(), "Command `" + args[2] + "` successfully added!");
    }

    //commands remove <commandName>
    @DiscordSubCommand(parent = "commands", name = "remove")
    private void commandsRemove(IMessage msg, String...args) {
        if (args.length < 3) {
            MessageSending.sendMessage(msg.getChannel(), "Not enough arguments.");
        } else if (SimpleTextCommandsSQL.removeCommand(msg.getGuild().getLongID(), args[2])) {
            MessageSending.sendMessage(msg.getChannel(), "`" + args[2] + "` successfully removed.");
        } else {
            MessageSending.sendMessage(msg.getChannel(), "`" + args[2] + "` is not a command");
        }
    }

    @DiscordSubCommand(parent = "commands", name = "edit")
    private void commandsEdit(IMessage msg, String...args) {
        if (args.length < 4) {
            MessageSending.sendMessage(msg.getChannel(), "Not enough arguments.");
        } else if (SimpleTextCommandsSQL.editCommand(msg.getGuild().getLongID(), args[2], msg.getContent().substring(msg.getContent().indexOf(args[2]) + args[2].length() + 1))) {
            String content = msg.getContent().substring(msg.getContent().indexOf(args[2]) + args[2].length() + 1);
            MessageSending.sendMessage(msg.getChannel(), "Changed `" + args[2] + "`!");
        } else {
            MessageSending.sendMessage(msg.getChannel(), "`" + args[2] + "` is not a command");
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
        String content = SimpleTextCommandsSQL.getCommandText(msg.getGuild().getLongID(), args[0]);
        if (content == null) {
            MessageSending.sendMessage(msg.getChannel(), "");
            return;
        }
        //TODO placeholder for args to be replaced by the arguments given when called
            /*
            * possible placeholders: author, args
            *
            *
            *
            * */
        content = content.replaceAll("§§AUTHOR§§", msg.getAuthor().mention(true));
        MessageSending.sendMessage(msg.getChannel(), content);
    }


}
