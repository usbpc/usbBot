package commands;

import sx.blah.discord.handle.obj.IMessage;

import java.lang.invoke.MethodHandle;

public class AnnotationCommand extends Command{
	private MethodHandle command;
	AnnotationCommand(String name, String description, MethodHandle command, CommandAccesChecker permission) {
		this.name = name;
		this.description = description;
		this.command = command;
		this.permission = permission;
	}

	@Override
	public void execute(IMessage msg, String...args) {
		if (this.permission.isAllowed(this, msg)) {
			try {
				command.invoke(msg, args);
			} catch (Throwable throwable) {
				msg.getChannel().sendMessage("Well that sure got me an Error... ```" + throwable.getMessage() + "```");
			}
		} else {
			msg.getChannel().sendMessage("You have no Permission to use this command.");
		}
	}
}
