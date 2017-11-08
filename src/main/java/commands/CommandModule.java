package commands;

import commands.core.Command;
import commands.core.CommandHandler;
import commands.security.PermissionManager;
import config.CommandPermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IRole;
import util.MessageSending;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * This Class should be used for all interaction with the Command and Permissions System.
 *
 * @author usbpc
 * @since 2017-04-28
 */

public class CommandModule implements DiscordCommands {
    private CommandHandler commandHandler;
    private PermissionManager permissionManager;
    private long guildID;
    private Logger logger = LoggerFactory.getLogger(CommandModule.class);

    public CommandModule(long guildID) {
        this.guildID = guildID;
        commandHandler = new CommandHandler(guildID);
        permissionManager = new PermissionManager();

    }

    /**
     * Registers the command so it can be executed from a discord message.
     *
     * @param command The command to register.
     */
    public void registerCommand(Command command) {
        commandHandler.registerCommand(command);
    }
    public void registerCommand(String commandName, Command command) {
        commandHandler.registerCommand(commandName, command);
    }

    /**
     * Registers the commands so they can be executed from a discord message.
     *
     * @param commands A Collection of commands to register.
     */
    public void registerCommands(Collection<Command> commands) {
        commands.forEach(this::registerCommand);
    }

    /**
     * Calls {@link DiscordCommands#getDiscordCommands()} and then registers the commands to be executed from a discord message.
     *
     * @param discordCommands A class that provides commands that should be executed from within discord.
     */
    public void registerCommands(DiscordCommands discordCommands) {
        registerCommands(discordCommands.getDiscordCommands());
    }

    /**
     * Unregisters the command with the given name so it can no longer be executed from a discord message.
     *
     * @param name The name of the command to unregister
     */
    public void unregisterCommand(String name) {
        commandHandler.unregisterCommand(name);
    }

    /**
     * Unregisters the commands with the given names so they can no longer be executed from a discord message.
     *
     * @param commands A Collection with the names of the commands to unregister
     */
    public void unregisterCommands(Collection<String> commands) {
        commands.forEach(this::unregisterCommand);
    }


    /*public void addRoleToCommandPermissions(String commandName, Long roleID) {
        if (getCommand(commandName) == null) throw new IllegalArgumentException(commandName + " is not a valid command");
        permissionManager.addRoleToPermission(commandName, roleID);
    }*/

    /**
     * Adds the given userID to the commandName permission list
     *
     * @param commandName The name of the command
     * @param userID The Snowflake user id as long
     */
    public void addUserToCommandPermission(String commandName, long userID) {
        if (commandHandler.getCommandByName(commandName) != null) {
            new CommandPermission(guildID, commandName).addUser(userID);
        } else {
            throw new IllegalArgumentException(commandName + " is not a registered command!");
        }
    }

    /**
     * Adds the given roleID to the commandName permission list
     *
     * @param commandName The name of the command
     * @param roleID The Snowflake role id as long
     */
    public void addRoleToCommandPermission(String commandName, long roleID) {
        logger.debug("[addRoleToCommandPermission] commandName: {} roleID: {}", commandName, roleID);
        if (commandHandler.getCommandByName(commandName) != null) {
            new CommandPermission(guildID, commandName).addRole(roleID);
        } else {
            throw new IllegalArgumentException(commandName + " is not a registered command!");
        }
    }

    /**
     * Checks if a specified command is registered
     * @param name Name of the Command
     * @return true if it is registered, false otherwise
     */
    public boolean discordCommandExists(String name) {
        return commandHandler.getCommandByName(name) != null;
    }

    public void runCommand(MessageReceivedEvent event) {

        //System.out.printf("[CommandModule] #%s @%s : %s\r\n", event.getChannel().getName(), event.getAuthor().getName(), event.getMessage().getContent());
        if (commandHandler.isCommand(event.getMessage().getContent())) {
            String[] args = commandHandler.getArguments(event.getMessage().getContent());
            if (PermissionManager.hasPermission(event.getGuild().getLongID(),
                    event.getAuthor().getLongID(),
                    event.getAuthor().getRolesForGuild(event.getGuild()).stream().map(IRole::getLongID).collect(Collectors.toSet()), args[0])
                    ||
                    event.getMessage().getGuild().getOwnerLongID() == event.getMessage().getAuthor().getLongID()) {
                commandHandler.runCommand(event.getMessage(), args);
            } else {
                MessageSending.sendMessage(event.getMessage().getChannel(), "You don't have permissions!");
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Command> getDiscordCommands() {
        Collection<Command> discordCommands = permissionManager.getDiscordCommands();
        discordCommands.addAll(commandHandler.getDiscordCommands());
        return discordCommands;
    }
}
