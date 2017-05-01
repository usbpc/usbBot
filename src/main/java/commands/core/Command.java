package commands.core;

import commands.security.Permission;
import sx.blah.discord.handle.obj.IMessage;

public abstract class Command {
	protected String name;
	String uuid;
	protected String description;


	abstract public void execute(IMessage msg, String...args);
	public String getName() {
		return name;
	}
}
