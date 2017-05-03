package modules;

import commands.annotations.DiscordCommand;
import config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MessageParsing;
import util.MessageSending;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

import java.io.ByteArrayInputStream;
import java.util.regex.Pattern;

public class TestCommands {
	private static Logger logger = LoggerFactory.getLogger(TestCommands.class);
	@DiscordCommand("regexquote")
	public void aregexquote(IMessage msg, String...args) {
		StringBuilder builder = new StringBuilder("This is a test");
		MessageSending.sendMessage(msg.getChannel(), builder.replace(builder.indexOf("is"), builder.indexOf("is") + "is".length(), "").toString());
	}
	@DiscordCommand("compare")
	public void compare(IMessage msg, String...args) {
		//MessageSending.sendMessage(msg.getChannel(), "This command just exists you you can't break my commands add command! :P");
		//MessageSending.sendMessage(msg.getChannel(), "The first index: " + "commands add".indexOf("add"));

		logger.debug("First arg: '{}' second arg: '{}'", args[1], args[2]);
		MessageSending.sendMessage(msg.getChannel(), "Does it match? " + args[1].matches(args[2]));
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
		MessageSending.sendMessage(msg.getChannel(), "There are the IDs I found: ```" + builder.toString() + "```");
	}
	@DiscordCommand("getavatarlink")
	public void getavatarlink(IMessage msg, String...args) {
		MessageSending.sendMessage(msg.getChannel(), msg.getClient().getUserByID(MessageParsing.getUserID((args[1]))).getAvatarURL());
	}
	@DiscordCommand("pruneamount")
	public void pruneamount(IMessage msg, String...args) {
		MessageSending.sendMessage(msg.getChannel(), "Amount of members to be pruned: " + msg.getGuild().getUsersToBePruned(Integer.valueOf(args[1])));
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
		MessageSending.sendFile(msg.getChannel(), "List of all users: ", new ByteArrayInputStream(builder.toString().getBytes()), "users.txt");

	}
	@DiscordCommand("getusername")
	public void getusername(IMessage msg, String...args) {
		IUser user = msg.getClient().getUserByID(MessageParsing.getUserID(args[1]));
		if (user == null) {
			MessageSending.sendMessage(msg.getChannel(), "Couldn't find user <@" + args[1] +">");
			return;
		}
		MessageSending.sendMessage(msg.getChannel(), "Name: " + user.getName());
	}
	@DiscordCommand("getrolename")
	public void getrolename(IMessage msg, String...args) {
		IRole role = msg.getClient().getRoleByID(MessageParsing.getGroupID(args[1]));
		if (role == null) {
			MessageSending.sendMessage(msg.getChannel(), "Couldn't find role `" + args[1] +"`");
			return;
		}
		MessageSending.sendMessage(msg.getChannel(), "Name: " + role.getName());
	}
}
