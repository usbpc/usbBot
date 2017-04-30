package modules;

import commands.*;
import config.Config;
import config.ConfigElement;
import main.Utils;
import sx.blah.discord.handle.obj.IMessage;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by usbpc on 30.04.2017.
 */
public class SimpleTextResponses {
    private Map<String, SimpleTextCommand> commands = new HashMap<>();

    CommandModule commandModule;
    public SimpleTextResponses(CommandModule commandModule) {
        this.commandModule = commandModule;
        Collection<DummyCommand> command = Config.getConfigByName("commands").getAllObjectsAs(DummyCommand.class);
        command.forEach(x -> {
            System.out.printf("[SimpleTextResponses] name: %s message: %s \r\n", x.name, x.message);
            commands.put(x.name, new SimpleTextCommand(x.name, x.message));
        });

        Collection<Command> cmds = commands.values().stream().map(simpleTextCommand -> (Command) simpleTextCommand).collect(Collectors.toCollection(HashSet::new));
        commandModule.registerCommands(cmds);
    }

    @DiscordCommand("commands")
    private int commands(IMessage msg, String...args) {
        if (args.length < 3) {
            Utils.sendMessage(msg.getChannel(), "Not enough arguments!");
            return -1;
        }
        return 0;
    }

    //commands add <commandName> <Message>
    @DiscordSubCommand(parent = "commands", name = "add")
    private void commandsAdd(IMessage msg, String...args) {
        if (args.length < 4) {
            Utils.sendMessage(msg.getChannel(), "Not enough arguments!");
            return;
        }
        if (commandModule.getCommand(args[2]) != null) {
            Utils.sendMessage(msg.getChannel(), "`" + args[2] + "` is already a command!");
            return;
        }


        String message = msg.getContent().substring(msg.getContent().indexOf(args[2]) + args[2].length() + 1);
        DummyCommand cmd = new DummyCommand(args[2], message);
        Config.getConfigByName("commands").putConfigElement(cmd);
        commandModule.registerCommand(new SimpleTextCommand(args[2], message));
        commandModule.addRoleToCommandPermissions(args[2], msg.getGuild().getEveryoneRole().getLongID());
        Utils.sendMessage(msg.getChannel(), "Command `" + args[2] + "` successfully added!");
    }

    //commands remove <commandName>
    @DiscordSubCommand(parent = "commands", name = "remove")
    private void commandsRemove(IMessage msg, String...args) {
        if (args.length < 3) {
            Utils.sendMessage(msg.getChannel(), "Not enough arguments.");
        } else if (!commands.containsKey(args[2])) {
            Utils.sendMessage(msg.getChannel(), "`" + args[2] + "` is not a command");
        } else {
            Config.getConfigByName("commands").removeConfigElement(args[2]);
            commandModule.unregisterCommand(args[2]);
            Utils.sendMessage(msg.getChannel(), "`" + args[2] + "` successfully removed.");
        }
    }

    @DiscordSubCommand(parent = "commands", name = "edit")
    private void commandsEdit(IMessage msg, String...args) {
        if (args.length < 4) {
            Utils.sendMessage(msg.getChannel(), "Not enough arguments.");
        } else if (!commands.containsKey(args[2])) {
            Utils.sendMessage(msg.getChannel(), "`" + args[2] + "` is not a command");
        } else {
            String content = msg.getContent().substring(msg.getContent().indexOf(args[2]) + args[2].length() + 1);
            commands.get(args[2]).content = content;
            Config.getConfigByName("commands").putConfigElement(new DummyCommand(args[2], content));
            Utils.sendMessage(msg.getChannel(), "Changed `" + args[2] + "`!");
        }
    }

    private class DummyCommand implements ConfigElement {
        String name;
        String message;
        private DummyCommand() {
        }
        DummyCommand(String name, String message) {
            this.name = name;
            this.message = message;
        }

        @Override
        public String getUUID() {
            return name;
        }

    }

    private class SimpleTextCommand extends Command {
        private String content;
        private SimpleTextCommand(String name, String content) {
            this.name = name;
            this.content = content;
            this.permission = new Permission("whitelist", new ArrayList<>(), "whitelist", new ArrayList<>());
        }
        @Override
        public void execute(IMessage msg, String... args) {
            Utils.sendMessage(msg.getChannel(), content);
        }
    }


}
