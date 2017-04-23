package commands;

import main.usbBot;
import sx.blah.discord.handle.obj.IMessage;

public class SimpleCommands {

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
