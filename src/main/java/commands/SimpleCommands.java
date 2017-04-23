package commands;

import main.usbBot;
import sx.blah.discord.handle.obj.IMessage;

public class SimpleCommands {
	@Command("ping")
	public static void ping(String[] args, IMessage msg) {
		if (msg.getAuthor().getLongID() == 1042141004221808641L) {
			msg.getChannel().sendMessage("<@" + msg.getAuthor().getLongID() + ">, I don't like you! :P");
		} else {
			msg.getChannel().sendMessage("pong!");
		}

	}

	@Command("hug")
	public static void hug(String[] args, IMessage msg) {
		msg.getChannel().sendMessage("_umarmt <@" + msg.getAuthor().getLongID() + ">_");
	}

	@Command("shutdown")
	public static void shutdown(String[] args, IMessage msg) {
		msg.getChannel().sendMessage("Shutting down...");
		usbBot.shutdown();
	}


}
