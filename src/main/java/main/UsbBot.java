package main;

import commands.*;
import config.ConfigObject;
import misc.BugEliros;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;

import java.io.*;
import java.util.Properties;

public class UsbBot {
	private IDiscordClient client;
	private ConfigObject configObject;
	UsbBot(String discordAPIKey) {



		File configFolder = new File("configs");
		File commands = new File(configFolder, "commands.json");


		configObject = new ConfigObject(commands).bindToProperty("systemCommands");

		CommandModule commandModule = new CommandModule(configObject);
		commandModule.registerCommandsFromObject(this);
		commandModule.registerCommandsFromObject(new TestCommands());
		//commandModule.registerCommandsFromObject(new SubCommandTest());*/

		client = createClient(discordAPIKey, false);
		client.getDispatcher().registerListener(new BugEliros());
		client.getDispatcher().registerListener(commandModule);
		client.login();

		//while (!client.isReady()) {

		//}

		//client.getUserByID(271071290692075520L).getOrCreatePMChannel().sendMessage("`Oh, gütige Herrscherin, ihr Freund hat soeben seinen Status geändert.` is what I would write you if <@105306394130968576> changed his status.");

	}

	public static void main(String...args) {
		new UsbBot(getDiscordAPIKey());
	}

	@DiscordCommand("shutdown")
	public void shutdown(IMessage msg, String...args) {
		//Logout the client when everything is done

		msg.getChannel().sendMessage("Shutting down...");
		Runtime.getRuntime().addShutdownHook(new Thread(() -> configObject.closeConnections()));
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
