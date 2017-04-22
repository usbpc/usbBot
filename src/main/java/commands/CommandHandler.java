package commands;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

public class CommandHandler {
	private Map<String, Method> commands = new HashMap<>();
	private Map<Method, Object> objectMap = new HashMap<>();

	void registerCommands(Object obj) {
		Class cl = obj.getClass();
		Arrays.stream(cl.getMethods()).filter(method -> method.isAnnotationPresent(Command.class))
				.peek(method -> objectMap.put(method, obj))
				.forEach(method -> commands.put(method.getAnnotation(Command.class).value(), method));
	}
	void registerCommands(Class cl) {
		Arrays.stream(cl.getMethods())
				//.peek(method -> System.out.printf("Methode name: %s\n", method.getName()))
				.filter(method -> method.isAnnotationPresent(Command.class))
				.peek(method -> objectMap.put(method, null))
				.peek(method -> System.out.printf("Metode name: %s, Command String: %s\n", method.getName(), method.getAnnotation(Command.class).value()))
				.forEach(method -> commands.put(method.getAnnotation(Command.class).value(), method));
	}

	boolean execute(String cmd) {
		if (commands.containsKey(cmd)) {
			Object response = null;
			Method m = commands.get(cmd);
			try {
				response = m.invoke(objectMap.get(m), "");
			} catch (IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
			}

			if (!(response instanceof String)) {
				return false;
			}

			String answer = (String) response;
			System.out.printf("Answer: %s \n", answer);

			return true;
		}
		return false;
	}
}
