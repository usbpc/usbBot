package main;

import commands.CommandHandler;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class EventHandler {
	private CommandHandler commandHandler;
	private usbBot bot;

	EventHandler(usbBot bot) {
		this.commandHandler = new CommandHandler(bot);
		this.bot = bot;
	}

	@EventSubscriber
	public void onMessageReceivedEvent(MessageReceivedEvent event) {
		if (CommandHandler.isCommand(event.getMessage().getContent())) {
			commandHandler.handleCommand(event.getMessage());

		}
	}
}
