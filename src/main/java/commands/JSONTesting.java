package commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JSONTesting {
	public static void main(String...args) throws IOException {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		List<SimpleCommand> list = new ArrayList<>();
		list.add(new SimpleCommand("ping", "pong!"));
		list.add(new SimpleCommand("heyo", "I am here!"));
		System.out.printf("%s\n", gson.toJson(list));

		File directory = new File("test");
		if (!directory.exists()) directory.mkdir();
		File file = new File(directory.getAbsolutePath() + "/test.txt");
		System.out.println(file.getAbsolutePath());
		FileWriter writer = new FileWriter(file);

		writer.write(gson.toJson(list));
		writer.close();
	}
}

