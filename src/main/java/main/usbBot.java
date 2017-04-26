package main;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import commands.*;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;

import java.io.*;
import java.util.Properties;

public class usbBot {
	private IDiscordClient client;

	usbBot(String discordAPIKey) {
		//Get a connection to discord and log in with the given API key
		client = createClient(discordAPIKey, true);

		File configFolder = new File("configs");
		File commands = new File(configFolder, "commands.json");

		JsonObject commandConfig = null;
		try (FileReader commandsReader = new FileReader(commands)) {
			commandConfig = new Gson().fromJson(commandsReader, JsonObject.class);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("CompleteFileJsonObject: " + commandConfig);
		CommandHandler commandHandler = new CommandHandler();
		PermissionManager permissionManager = new PermissionManager(commandHandler, commandConfig.getAsJsonArray("systemCommands"));
		CommandRegisterHelper commandRegisterHelper = new CommandRegisterHelper(permissionManager);

		commandHandler.registerCommands(commandRegisterHelper.getCommandList(commandHandler));
		commandHandler.registerCommands(commandRegisterHelper.getCommandList(this));
		commandHandler.registerCommands(commandRegisterHelper.getCommandList(permissionManager));
		commandHandler.registerCommands(commandRegisterHelper.getCommandList(new TestCommands()));

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
