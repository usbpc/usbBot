package commands;

import sx.blah.discord.handle.obj.IMessage;

public interface CommandAccesChecker {
	public boolean isAllowed(Command command, IMessage message);

}
