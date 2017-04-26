package commands;

import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.Permissions;

import java.lang.invoke.MethodHandle;

public class AnnotationCommand extends Command{
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
