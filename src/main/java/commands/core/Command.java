package commands.core;

import commands.security.Permission;
import sx.blah.discord.handle.obj.IMessage;

public abstract class Command {
	protected String name;
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
	public String getName() {
		return name;
	}
}
