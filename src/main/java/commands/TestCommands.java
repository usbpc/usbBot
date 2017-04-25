package commands;

import sx.blah.discord.handle.obj.IMessage;

public class TestCommands {
	@DiscordCommand("ping")
	public void ping(IMessage msg, String...args) {
		msg.getChannel().sendMessage("pong!");
	}
	@DiscordCommand("deleteme")
	public void deleteme(IMessage msg, String...args) {
		msg.delete();
	}
}
