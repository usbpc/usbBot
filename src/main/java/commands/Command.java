package commands;

import sx.blah.discord.handle.obj.IMessage;

public abstract class Command {
	String name;
	String uuid;
	protected String description;
	protected Permission permission;

	abstract public void execute(IMessage msg, String...args);
	public void setPermission(Permission permission) {
		this.permission = permission;
	}
	public Permission getPermission() {
		return permission;
	}
}
