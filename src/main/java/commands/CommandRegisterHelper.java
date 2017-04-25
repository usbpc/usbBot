package commands;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandRegisterHelper {
	public static List<Command> getCommands(Object obj) {
		Class cl = obj.getClass();
		List<Command> commands = new ArrayList<>();
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		Arrays.stream(cl.getMethods()).filter(method -> method.isAnnotationPresent(DiscordCommand.class))
				.forEach(method -> {
					try {
						commands.add(new AnnotationCommand(method.getAnnotation(DiscordCommand.class).value(),"",lookup.unreflect(method).bindTo(obj), new CreatorOnlyPermission()));
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				});
		return commands;
	}
}
