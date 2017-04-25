package commands;

import sx.blah.discord.handle.obj.IMessage;

public abstract class Command {
	String name;
	protected String description;
	protected CommandAccesChecker permission;

	abstract public void execute(IMessage msg, String...args);
	public void setPermission(CommandAccesChecker permission) {
		this.permission = permission;
	}
	public CommandAccesChecker getPermission() {
		return permission;
	}
}
