package commands;

import sx.blah.discord.handle.obj.IMessage;

public class CreatorOnlyPermission implements CommandAccesChecker {
	@Override
	public boolean isAllowed(Command command, IMessage message) {
		return message.getAuthor().getLongID() == 104214100422180864L;
	}
}
