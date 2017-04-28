package commands;

import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.Permissions;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.stream.Collectors;

public class CommandRegisterHelper {
	private PermissionManager commandPermissions;

	public CommandRegisterHelper(PermissionManager commands) {
		commandPermissions = commands;
	}

	public List<Command> getCommandList(Object obj) {
		Class cl = obj.getClass();
		List<Command> commands = new ArrayList<>();

		Map<String, TmpCommandContainer> commandMap = new HashMap<>();

		MethodHandles.Lookup lookup = MethodHandles.lookup();
				Arrays.stream(cl.getMethods()).filter(x -> x.isAnnotationPresent(DiscordCommand.class)).forEach(x -> {
			try {
				commandMap.put(x.getName(), new TmpCommandContainer(x.getAnnotation(DiscordCommand.class).value(), null ,lookup.unreflect(x).bindTo(obj)));
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		});
		Arrays.stream(cl.getMethods()).filter(x -> x.isAnnotationPresent(DiscordSubCommand.class)).forEach(x -> {
			try {
				commandMap.put(x.getName(), new TmpCommandContainer(x.getAnnotation(DiscordSubCommand.class).name(), x.getAnnotation(DiscordSubCommand.class).parent() ,lookup.unreflect(x).bindTo(obj)));
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		});
		commandMap.values().stream().filter(x -> x.parentCommand != null).forEach(x -> {
			if (commandMap.containsKey(x.parentCommand)) {
				commandMap.get(x.parentCommand).hasChildren = true;
			}
		});

		//commandMap.forEach((name, cmdContainer) -> System.out.printf("%s: children %b, parent is %s\r\n", name, cmdContainer.hasChildren, cmdContainer.parentCommand));
		//This regeisters all commands that don't have sub commands and removes them from the commandMap Map
		Set<String> noSubCommands = new HashSet<>();
		commandMap.entrySet().stream().filter(x -> !x.getValue().hasChildren && x.getValue().parentCommand == null)
				.forEach(x -> {
					noSubCommands.add(x.getKey());
					commands.add(new AnnotationCommand(x.getValue().name, "", x.getValue().command, commandPermissions.getPermissionByName(x.getValue().name)));
				});
		noSubCommands.forEach(commandMap::remove);

		//This is the start of my code to build the sub command tree thingy
		while (true) {
			TmpCommandContainer currDeepest = null;
			int currMaxDepth = 0;
			for (TmpCommandContainer x : commandMap.values().stream().filter(x -> x.parentCommand != null && !x.hasChildren).collect(Collectors.toCollection(HashSet::new))) {
				TmpCommandContainer tmp = x;
				int depthCount = 0;
				while (tmp.parentCommand != null) {
					tmp = commandMap.get(tmp.parentCommand);
					depthCount++;
				}
				if (depthCount > currMaxDepth) {
					currMaxDepth = depthCount;
					currDeepest = x;
				}
			}

			if (currDeepest == null) break;

			String curSearchParentName = currDeepest.parentCommand;
			TmpCommandContainer currParent = commandMap.get(curSearchParentName);
			currParent.subCommands = new HashMap<>();

			System.out.printf("Currently searching for subcommand of %s \r\n", curSearchParentName);

			for (Map.Entry<String, TmpCommandContainer> entry : commandMap.entrySet().stream()
					.filter(x -> x.getValue().parentCommand != null)
					.filter(x -> x.getValue().parentCommand.equals(curSearchParentName))
					.collect(Collectors.toCollection(HashSet::new)))
			{
				TmpCommandContainer sameParent = entry.getValue();
				currParent.subCommands.put(sameParent.name, new SubCommand(sameParent.command, sameParent.subCommands));
				commandMap.remove(entry.getKey());
			}

			currParent.hasChildren = false;
		}

		commandMap.values().forEach(x -> commands.add(new SubCommandParent(x.name, "", x.command, commandPermissions.getPermissionByName(x.name), x.subCommands)));


		return commands;
	}
	private class TmpCommandContainer {
		Map<String, SubCommand> subCommands = null;
		boolean hasChildren = false;
		String name;
		String parentCommand;
		MethodHandle command;

		TmpCommandContainer(String name, String parentCommand, MethodHandle command) {
			this.name = name;
			this.command = command;
			this.parentCommand = parentCommand;
		}
	}
	private class SubCommand {
		private MethodHandle command;
		Map<String, SubCommand> subCommandMap;

		SubCommand(MethodHandle command, Map<String, SubCommand> subCommandMap) {
			this.command = command;
			this.subCommandMap = subCommandMap;
		}

		public void execute(IMessage msg, String[] args, int depth) {
			try {
				String[] newArgs = (String[]) command.invoke(msg, args);
				if (subCommandMap != null) {
					if (args.length > depth + 1) {
						if (subCommandMap.containsKey(newArgs[depth + 1])) {
							subCommandMap.get(newArgs[depth + 1]).execute(msg, newArgs, depth + 1);
						} else {
							StringBuilder builder = new StringBuilder();
							builder.append("The Subcommand `").append(newArgs[depth + 1]).append("` of command `");
							for (int i = 0; i <= depth; i++) {
								builder.append(newArgs[i]).append(' ');
							}
							builder.append("` dosen't exist");

							msg.getChannel().sendMessage(builder.toString());
						}

					} else {
						msg.getChannel().sendMessage("You need to give me more, please!");
					}

				}
			} catch (Throwable throwable) {
				if (msg.getChannel().getModifiedPermissions(msg.getClient().getOurUser()).contains(Permissions.SEND_MESSAGES)) {
					msg.getChannel().sendMessage("Well that sure got me an Error... ```" + throwable.getMessage() + "```");
				} else {
					throwable.printStackTrace();
					System.out.println("Well I got an Error AND don't have permission to write in the channel I wanna write to... " + throwable.getMessage());
				}
			}
		}
	}
	private class SubCommandParent extends Command {
		private MethodHandle command;
		Map<String, SubCommand> subCommandMap;

		SubCommandParent(String name, String description, MethodHandle command, Permission permission, Map<String, SubCommand> subCommandMap) {
			this.name = name;
			this.description = description;
			this.command = command;
			this.permission = permission;
			this.subCommandMap = subCommandMap;
		}

		@Override
		public void execute(IMessage msg, String... args) {
			if (permission.isAllowed(msg)) {
				try {
					String[] newArgs = (String[]) command.invoke(msg, args);
					subCommandMap.get(newArgs[1]).execute(msg, newArgs, 1);
				} catch (Throwable throwable) {
					if (msg.getChannel().getModifiedPermissions(msg.getClient().getOurUser()).contains(Permissions.SEND_MESSAGES)) {
						msg.getChannel().sendMessage("Well that sure got me an Error... ```" + throwable.getMessage() + "```");
					} else {
						throwable.printStackTrace();
						System.out.println("Well I got an Error AND don't have permission to write in the channel I wanna write to... " + throwable.getMessage());
					}
				}
			}
		}
	}
	private class AnnotationCommand extends Command {
		private MethodHandle command;
		AnnotationCommand(String name, String description, MethodHandle command, Permission permission) {
			this.name = name;
			this.description = description;
			this.command = command;
			this.permission = permission;
		}

		@Override
		public void execute(IMessage msg, String...args) {
			if (permission.isAllowed(msg)) {
				try {
					command.invoke(msg, args);
				} catch (Throwable throwable) {
					if (msg.getChannel().getModifiedPermissions(msg.getClient().getOurUser()).contains(Permissions.SEND_MESSAGES)) {
						msg.getChannel().sendMessage("Well that sure got me an Error... ```" + throwable.getMessage() + "```");
					} else {
						System.out.println("Well I got an Error AND don't have permission to write in the channel I wanna write to... " + throwable.getMessage());
					}

				}
			} else {
				msg.getChannel().sendMessage("You have no Permission to use this command.");
			}
		}
	}
}
