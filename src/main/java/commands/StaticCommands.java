package commands;

import main.usbBot;
import sx.blah.discord.handle.obj.IMessage;

public class StaticCommands {

	@Command("deleteme")
	public static void deleteMe(String[] args, IMessage msg) {
		msg.delete();
	}

}
