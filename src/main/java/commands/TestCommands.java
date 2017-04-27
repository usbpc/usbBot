package commands;

import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.MessageBuilder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
	@DiscordCommand("getavatarlink")
	public void getavatarlink(IMessage msg, String...args) {
		msg.getChannel().sendMessage(msg.getAuthor().getAvatarURL());
	}
	@DiscordCommand("pruneamount")
	public void pruneamount(IMessage msg, String...args) {
		msg.getChannel().sendMessage("Amount of members to be pruned: " + msg.getGuild().getUsersToBePruned(Integer.valueOf(args[1])));
	}

	@DiscordCommand("getuserids")
	public void getuserids(IMessage msg, String...args) {
		StringBuilder builder = new StringBuilder();
		msg.getGuild().getUsers().forEach(user -> builder.append(user.getName()).append(": ").append(user.getLongID()).append("\r\n"));
		MessageBuilder msgBuilder = new MessageBuilder(msg.getClient());

		msgBuilder.withChannel(msg.getChannel()).withFile(new ByteArrayInputStream(builder.toString().getBytes()), "test.txt").build();


	}
}
