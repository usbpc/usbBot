package commands.core;

import commands.DiscordCommands;
import util.commands.AnnotationExtractor;
import util.commands.DiscordCommand;
import util.MessageSending;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.handle.obj.IMessage;

import java.util.*;
import java.util.regex.Pattern;

public class CommandHandler implements DiscordCommands{
	private Map<String, Command> commands = new HashMap<>();
	//TODO make this a config option
	private String PREFIX = "!";
	private Logger logger = LoggerFactory.getLogger(CommandHandler.class);
	private StringBuilder cmdPattern = new StringBuilder();
	public CommandHandler() {
	}

	public void registerCommand(Command cmd) {
		commands.put(cmd.getName(), cmd);
		if (cmdPattern.length() > 1) {
			cmdPattern.append('|');
		}
		cmdPattern.append(Pattern.quote(cmd.getName()));
	}

	public void registerCommand(String commandName, Command cmd) {
		commands.put(commandName, cmd);
		if (cmdPattern.length() > 1) {
			cmdPattern.append('|');
		}
		cmdPattern.append(Pattern.quote(commandName));
	}

	public void unregisterCommand(String name) {
		if (!commands.containsKey(name)) throw new IllegalArgumentException(name + " is not a valid command");
		commands.remove(name);
		name = Pattern.quote(name);
		//This craziness needs to be in place in order to allow commands to contain all characters including ones that are used by regex
		cmdPattern.replace(cmdPattern.indexOf(name), cmdPattern.indexOf(name) + name.length(), "");

		if (cmdPattern.charAt(0) == '|') {
			cmdPattern.deleteCharAt(0);
		} else if (cmdPattern.charAt(cmdPattern.length() - 1) == '|') {
			cmdPattern.deleteCharAt(cmdPattern.length() - 1);
		} else {
			int check = cmdPattern.indexOf("||");
			if (check > 0) {
				cmdPattern.replace(check, check + 2, "|");
			}
		}

	}

	public void runCommand(IMessage message, String[] digestedString) {
		if (!message.getChannel().isPrivate() && isCommand(message.getContent())) {
			if (commands.containsKey(digestedString[0])) {

				logger.debug("Executing command '{}'", digestedString[0]);
				//System.out.printf("[CommandHandler] Executing command '%s'\r\n", digestedString[0]);
				commands.get(digestedString[0]).execute(message, digestedString);
			} /*else {
				message.getChannel().sendMessage("Command `" + PREFIX + digestedString[0] + "` not found.");
			}*/

		}
	}

	@DiscordCommand("list")
	public void list(IMessage msg, String...args) {
		Iterator<String> iterator = commands.keySet().iterator();
		String commands = "";
		StringBuilder builder = new StringBuilder();
		while (iterator.hasNext()) {
			builder.append('!').append(iterator.next());
			if (iterator.hasNext()) {
				builder.append(", ");
			} else {
				commands = builder.toString();
			}
		}
		MessageSending.sendMessage(msg.getChannel(), "Commands are: " + commands);

	}

	public Command getCommandByName(String name) {
		return commands.get(name);
	}

	public Collection<Command> getAllCommands() {
		return commands.values();
	}

	public boolean isCommand(String str) {
		return str.matches(" *" + Pattern.quote(PREFIX) + '(' +cmdPattern.toString() + ')' + ".*");
	}
	public String[] getArguments(String input) {
		return input.substring(input.indexOf(PREFIX) + 1).split(" +");
	}

	@Override
	public Collection<Command> getDiscordCommands() {
		return AnnotationExtractor.getCommandList(this);
	}
}
