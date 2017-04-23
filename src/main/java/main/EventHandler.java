package main;

import commands.CommandHandler;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class EventHandler {
	CommandHandler commandHandler;

	EventHandler() {
		this.commandHandler = new CommandHandler();
	}

	@EventSubscriber
	public void onMessageReceivedEvent(MessageReceivedEvent event) {
		if (CommandHandler.isCommand(event.getMessage().getContent())) {
			commandHandler.handleCommand(event.getMessage());
		}
	}
}
