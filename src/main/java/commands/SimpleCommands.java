package commands;

public class SimpleCommands {
	@Command("ping")
	public static String ping(String Message) {
		return "pong";
	}

}
