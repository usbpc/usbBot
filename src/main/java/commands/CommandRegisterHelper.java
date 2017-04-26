package commands;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.invoke.MethodHandles;
import java.util.*;

public class CommandRegisterHelper {
	private Map<String, Permission> commandPermissions = new HashMap<>();

	public CommandRegisterHelper(JsonArray commands) {
		Gson gson = new Gson();
		for(JsonElement element : commands) {
			JsonObject jsonObject = element.getAsJsonObject();

			commandPermissions.put(jsonObject.getAsJsonPrimitive("name").getAsString(), gson.fromJson(jsonObject.getAsJsonObject("permission"), Permission.class));
		}
	}

	public List<Command> getCommands(Object obj) {
		Class cl = obj.getClass();
		List<Command> commands = new ArrayList<>();
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		Arrays.stream(cl.getMethods()).filter(method -> method.isAnnotationPresent(DiscordCommand.class))
				.forEach(method -> {
					try {
						String name = method.getAnnotation(DiscordCommand.class).value();
						commands.add(new AnnotationCommand(name,"",lookup.unreflect(method).bindTo(obj), commandPermissions.get(name)));
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				});
		return commands;
	}
}
