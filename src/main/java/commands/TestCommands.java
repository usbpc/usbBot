package commands;

import main.Utils;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.MessageBuilder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class TestCommands {
	@DiscordCommand("ping")
	public void ping(IMessage msg, String...args) {
		msg.getChannel().sendMessage("Pong!");
	}
	@DiscordCommand("deletereactions")
	public void deletereactions(IMessage msg, String...args) {
		if (args[1].matches("\\d{18,19}+")) {
			IMessage toDelete = msg.getChannel().getMessageByID(Long.valueOf(args[1]));
			if (toDelete != null) {
				toDelete.removeAllReactions();
			}
		}
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
		msg.getGuild().getUsers().forEach(user -> builder/*.append(user.getName()).append(": ")*/.append(user.getLongID()).append("\r\n"));
		MessageBuilder msgBuilder = new MessageBuilder(msg.getClient());

		msgBuilder.withChannel(msg.getChannel()).withFile(new ByteArrayInputStream(builder.toString().getBytes()), "test.txt").build();

	}
	@DiscordCommand("getusername")
	public void getusername(IMessage msg, String...args) {
		IUser user = Utils.getUser(msg.getGuild(), args[1]);
		if (user == null) {
			msg.getChannel().sendMessage("Couldn't find user <@" + args[1] +">");
			return;
		}
		msg.getChannel().sendMessage("Name: " + user.getName());
	}
	@DiscordCommand("getrolename")
	public void getrolename(IMessage msg, String...args) {
		IRole role = Utils.getRole(msg.getGuild(), args[1]);
		if (role == null) {
			msg.getChannel().sendMessage("Couldn't find role `" + args[1] +"`");
			return;
		}
		msg.getChannel().sendMessage("Name: " + role.getName());
	}
}
