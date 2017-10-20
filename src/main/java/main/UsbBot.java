package main;

import commands.*;
import commands.core.Command;
import config.DatabaseConnection;
import util.commands.AnnotationExtractor;
import util.commands.DiscordCommand;
import config.Config;
import modules.SimpleTextResponses;
import modules.TestCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import util.MessageSending;

import java.io.*;
import java.util.Collection;
import java.util.Properties;

public class UsbBot implements DiscordCommands {
	private static Logger logger = LoggerFactory.getLogger(UsbBot.class);
	private IDiscordClient client;
	private UsbBot(String discordAPIKey) {

		client = createClient(discordAPIKey, false);
		client.getDispatcher().registerListener(new EventHandler(this));
		client.login();

	}

	public static void main(String...args) {
		new UsbBot(getDiscordAPIKey());
	}

	@DiscordCommand("shutdown")
	public void shutdown(IMessage msg, String...args) {
		//Logout the client when everything is done

		MessageSending.sendMessage(msg.getChannel(), "Shutting down...");
		Runtime.getRuntime().addShutdownHook(new Thread(Config::close));
		Runtime.getRuntime().addShutdownHook(new Thread(DatabaseConnection::closeConnection));
		client.logout();

	}

	private static String getDiscordAPIKey() {
		Properties keys = new Properties();
		String discordApiKey = "404";
		try {
			FileInputStream in = new FileInputStream("keys.properties");
			keys.load(in);
			discordApiKey = keys.getProperty("discord", "404");
			if (discordApiKey.equals("404")) {
				FileOutputStream out = new FileOutputStream("keys.properties");

				logger.error("No discord API key found, please enter one in the keys.properties file");
				keys.setProperty("discord", "404");
				keys.store(out, "");
				System.exit(-1);
			}
		} catch (IOException e) {
			logger.error("Something went wrong while opening the keys.properties", e);
		}
		return discordApiKey;
	}

	private static IDiscordClient createClient(String token, boolean login) {
		ClientBuilder clientBuilder = new ClientBuilder();
		clientBuilder.withToken(token);
		try {
			if (login) {
				return clientBuilder.login();
			} else {
				return clientBuilder.build();
			}
		} catch (DiscordException e) {
			logger.error("Building the client failed ", e);
			return null;
		}
	}

	@Override
	public Collection<Command> getDiscordCommands() {
		return AnnotationExtractor.getCommandList(this);
	}
}
