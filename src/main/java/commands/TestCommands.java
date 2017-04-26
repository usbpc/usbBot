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

	@DiscordCommand("getroleids")
	public void getroleids(IMessage msg, String...args) {
		StringBuilder builder = new StringBuilder();
		msg.getGuild().getRoles().forEach(role -> builder.append(role.getName()).append(": ").append(role.getLongID()).append('\n'));
		msg.getChannel().sendMessage("There are the IDs I found: ```" + builder.toString() + "```");
	}
}
