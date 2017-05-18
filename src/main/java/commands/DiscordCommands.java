package commands;

import commands.core.Command;

import java.util.Collection;

/**
 * This interface should be implemented by all Classes that intend to provide commands accessible from within discord.
 *
 * @author usbpc
 * @since 2017-05-18
 */
public interface DiscordCommands {

    /**
     *
     * @return Collection containing all {@link commands.core.Command Commands} provided.
     */
    public Collection<Command> getDiscordCommands();

}
