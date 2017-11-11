package usbbot.commands;

import usbbot.commands.core.Command;

import java.util.Collection;

/**
 * This interface should be implemented by all Classes that intend to provide usbbot.commands accessible from within discord.
 *
 * @author usbpc
 * @since 2017-05-18
 */
public interface DiscordCommands {

    /**
     *
     * @return Collection containing all {@link usbbot.commands.core.Command Commands} provided.
     */
    //TODO: change Collection to Iterable
    public Collection<Command> getDiscordCommands();

}
