package commands;

import config.Config;
import main.Utils;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.MessageBuilder;

import javax.rmi.CORBA.Util;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class TestCommands {

	@DiscordCommand("teststuff")
	public void add(IMessage msg, String...args) {
		//Utils.sendMessage(msg.getChannel(), "This command just exists you you can't break my commands add command! :P");
		//Utils.sendMessage(msg.getChannel(), "The first index: " + "commands add".indexOf("add"));

		Config.close();
		Utils.sendMessage(msg.getChannel(), "Saved the world!");
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
		Utils.sendMessage(msg.getChannel(), "There are the IDs I found: ```" + builder.toString() + "```");
	}
	@DiscordCommand("getavatarlink")
	public void getavatarlink(IMessage msg, String...args) {
		Utils.sendMessage(msg.getChannel(), Utils.getUser(msg.getGuild(), args[1]).getAvatarURL());
	}
	@DiscordCommand("pruneamount")
	public void pruneamount(IMessage msg, String...args) {
		Utils.sendMessage(msg.getChannel(), "Amount of members to be pruned: " + msg.getGuild().getUsersToBePruned(Integer.valueOf(args[1])));
	}

	@DiscordCommand("getuserids")
	public void getuserids(IMessage msg, String...args) {

		StringBuilder builder = new StringBuilder();
		msg.getGuild().getUsers().forEach(user -> builder.append(user.getName()).append(": ").append(user.getLongID()).append("\r\n"));
		builder.deleteCharAt(builder.length() - 1);
		builder.deleteCharAt(builder.length() - 1);
		//MessageBuilder msgBuilder = new MessageBuilder(msg.getClient());

		//msgBuilder.withChannel(msg.getChannel()).withFile(new ByteArrayInputStream(builder.toString().getBytes()), "test.txt").build();

		//msg.getChannel().sendFile("List of all users: ", new ByteArrayInputStream(builder.toString().getBytes()), "users.txt");
		Utils.sendFile(msg.getChannel(), "List of all users: ", new ByteArrayInputStream(builder.toString().getBytes()), "users.txt");

	}
	@DiscordCommand("getusername")
	public void getusername(IMessage msg, String...args) {
		IUser user = Utils.getUser(msg.getGuild(), args[1]);
		if (user == null) {
			Utils.sendMessage(msg.getChannel(), "Couldn't find user <@" + args[1] +">");
			return;
		}
		Utils.sendMessage(msg.getChannel(), "Name: " + user.getName());
	}
	@DiscordCommand("getrolename")
	public void getrolename(IMessage msg, String...args) {
		IRole role = Utils.getRole(msg.getGuild(), args[1]);
		if (role == null) {
			Utils.sendMessage(msg.getChannel(), "Couldn't find role `" + args[1] +"`");
			return;
		}
		Utils.sendMessage(msg.getChannel(), "Name: " + role.getName());
	}
}
