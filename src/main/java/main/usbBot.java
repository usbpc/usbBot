package main;

import commands.CommandHandler;
import commands.DiscordCommand;
import commands.TestCommands;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class usbBot {
	private IDiscordClient client;

	usbBot(String discordAPIKey) {
		//Get a connection to discord and log in with the given API key
		client = createClient(discordAPIKey, true);

		CommandHandler commandHandler = new CommandHandler();
		commandHandler.registerCommands(this);
		commandHandler.registerCommands(new TestCommands());

		client.getDispatcher().registerListener(commandHandler);

		//Waiting for the client to be ready before continuing
		while (!client.isReady()) {
		}
	}

	public static void main(String...args) {
		new usbBot(getDiscordAPIKey());
	}

	@DiscordCommand("shutdown")
	public void shutdown(IMessage msg, String...args) {
		//Logout the client when everything is done
		msg.getChannel().sendMessage("Shutting down...");
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
				System.out.println("No discord API key found, please enter one in the keys.properties file");
				keys.setProperty("discord", "404");
				keys.store(out, "");
				System.exit(-1);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return discordApiKey;
	}

	public static IDiscordClient createClient(String token, boolean login) {
		ClientBuilder clientBuilder = new ClientBuilder();
		clientBuilder.withToken(token);
		try {
			if (login) {
				return clientBuilder.login();
			} else {
				return clientBuilder.build();
			}
		} catch (DiscordException e) {
			e.printStackTrace();
			return null;
		}
	}
}
