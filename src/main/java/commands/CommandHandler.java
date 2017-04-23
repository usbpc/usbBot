package commands;

import sx.blah.discord.handle.obj.IMessage;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CommandHandler {
	private Map<String, MethodHandle> commands = new HashMap<>();
	static private final String PREFIX = "!";

	public CommandHandler() {
		CommandRegister.register(this);
	}

	void registerCommands(Object obj, Class cl) {
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		Arrays.stream(cl.getMethods()).filter(method -> method.isAnnotationPresent(Command.class))
				.forEach(method -> {
					try {
						if (obj == null) {
							commands.put(method.getAnnotation(Command.class).value(), lookup.unreflect(method));
						} else {
							commands.put(method.getAnnotation(Command.class).value(), lookup.unreflect(method).bindTo(obj));
						}
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				});
	}
	void registerCommands(Class cl) {
		registerCommands(null, cl);
	}
	void registerCommands(Object obj) {
		registerCommands(obj, obj.getClass());
	}
	void registerSimpleCommand(SimpleCommand cmd) {
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		try {
			commands.put(cmd.name, lookup.unreflect(cmd.getClass().getDeclaredMethod("execute", String[].class, IMessage.class)).bindTo(cmd));
		} catch (IllegalAccessException | NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

	public void handleCommand(IMessage message) {
		String msg = message.getContent();
		String[] digestedString = msg.substring(msg.indexOf(PREFIX) + 1).split(" ");
		String cmd = digestedString[0];
		String[] args = Arrays.copyOfRange(digestedString, 1, digestedString.length);

		executeCommand(cmd, args, message);
	}

	private void executeCommand(String cmd, String[] args, IMessage msg) {
		if (commands.containsKey(cmd)) {
			MethodHandle m = commands.get(cmd);

			try {
				m.invoke(args, msg);
			} catch (Throwable throwable) {
				throwable.printStackTrace();
			}


		} else {
			msg.getChannel().sendMessage("Command \"" + PREFIX + cmd + "\" not found.");
		}
	}

	public static boolean isCommand(String str) {
		return str.matches(" *" + PREFIX + ".*");
	}
}
