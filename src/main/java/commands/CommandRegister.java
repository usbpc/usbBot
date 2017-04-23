package commands;

public class CommandRegister {
	public static void main(String...args) {
		CommandHandler handler = new CommandHandler();

		handler.registerCommands(SimpleCommands.class);

	}
}
