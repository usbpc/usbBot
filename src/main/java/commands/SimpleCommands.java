package commands;

import main.usbBot;
import sx.blah.discord.handle.obj.IMessage;

public class SimpleCommands {
	@Command("ping")
	public static String ping(String[] args, IMessage msg) {
		return "pong";
	}

	@Command("shutdown")
	public static String shutdown(String[] args, IMessage msg) {
		usbBot.shutdown();
		return "Shutting down...";
	}


}
