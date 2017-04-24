package commands;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import main.usbBot;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class CommandRegister {
	public static void register(CommandHandler handler, usbBot bot) {
		handler.registerCommands(StaticCommands.class);
		handler.registerCommands(new Commands(handler, bot));
		try {
			loadSimpleCommands(handler);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private static void loadSimpleCommands(CommandHandler handler) throws IOException {
		File directory = new File("configs");
		if (!directory.exists()) directory.mkdir();
		File cmds = new File(directory.getAbsolutePath() + "/commands.json");
		if (!cmds.exists()) {
			FileWriter writer = new FileWriter(cmds);
			writer.write("[{\"name\":\"ping\",\"response\":\"pong!\"},{\"name\":\"heyo\",\"response\":\"I am here!\"}]");
			writer.close();
		}
		Gson gson = new Gson();
		Type listType = new TypeToken<List<SimpleCommand>>(){}.getType();
		List<SimpleCommand> list = gson.fromJson(new FileReader(cmds), listType);

		list.stream().forEach(x -> handler.registerSimpleCommand(x));
	}
}
