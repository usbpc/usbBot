package commands;

import main.usbBot;
import sx.blah.discord.handle.obj.IMessage;

import java.util.Iterator;
import java.util.Set;

public class Commands {
	private final usbBot bot;
	private final CommandHandler handler;
	Commands(CommandHandler handler, usbBot bot) {
		this.bot = bot;
		this.handler = handler;
	}
	@Command("shutdown")
	public void shutdown(String[] args, IMessage msg) {
		msg.getChannel().sendMessage("Shutting down...");
		bot.shutdown();
	}

	@Command("list")
	public void list(String[] args, IMessage msg) {
		Iterator<String> iterator = handler.getRegisteredCommands().iterator();
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
		msg.getChannel().sendMessage("Commands are: " + commands);
	}
}
