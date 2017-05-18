package commands;

import util.commands.AnnotationExtractor;
import commands.core.Command;
import commands.core.CommandHandler;
import commands.security.PermissionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IRole;
import util.MessageSending;

import java.util.Collection;
import java.util.stream.Collectors;

public class CommandModule implements DiscordCommands {
    private CommandHandler commandHandler;
    private PermissionManager permissionManager;
    private Logger logger = LoggerFactory.getLogger(CommandModule.class);
    //TODO add more access to permissions stuff

    public CommandModule() {
        commandHandler = new CommandHandler();
        permissionManager = new PermissionManager();

    }

    public void registerCommand(Command command) {
        commandHandler.registerCommand(command);
    }

    public void registerCommands(Collection<Command> commands) {
        commands.forEach(this::registerCommand);
    }

    public void registerCommands(DiscordCommands obj) {
        registerCommands(obj.getDiscordCommands());
    }

    public void unregisterCommand(String name) {
        commandHandler.unregisterCommand(name);
        permissionManager.removePermissions(name);
    }

    public void unregisterCommands(Collection<String> commands) {
        commands.forEach(this::unregisterCommand);
    }


    /*public void addRoleToCommandPermissions(String commandName, Long roleID) {
        if (getCommand(commandName) == null) throw new IllegalArgumentException(commandName + " is not a valid command");
        permissionManager.addRoleToPermission(commandName, roleID);
    }*/
    public void addUserToCommandPermission(String commandName, long userID) {
        if (commandHandler.getCommandByName(commandName) != null) {
            permissionManager.addUser(commandName, userID);
        } else {
            throw new IllegalArgumentException(commandName + " is not a registered command!");
        }
    }

    public void addRoleToCommandPermission(String commandName, long roleID) {
        logger.debug("[addRoleToCommandPermission] commandName: {} roleID: {}", commandName, roleID);
        if (commandHandler.getCommandByName(commandName) != null) {
            permissionManager.addRole(commandName, roleID);
        } else {
            throw new IllegalArgumentException(commandName + " is not a registered command!");
        }
    }

    public Collection<Command> getAllCommands() {
        return commandHandler.getAllCommands();
    }

    public Command getCommand(String uuid) {
        return commandHandler.getCommandByName(uuid);
    }

    @EventSubscriber
    public void runCommand(MessageReceivedEvent event) {

        logger.debug("#{} @{} : {}", event.getChannel().getName(), event.getAuthor().getName(), event.getMessage().getContent());
        //System.out.printf("[CommandModule] #%s @%s : %s\r\n", event.getChannel().getName(), event.getAuthor().getName(), event.getMessage().getContent());
        if (commandHandler.isCommand(event.getMessage().getContent())) {
            String[] args = commandHandler.getArguments(event.getMessage().getContent());
            if (permissionManager.hasPermission(event.getAuthor().getLongID(), event.getAuthor().getRolesForGuild(event.getGuild()).stream().map(IRole::getLongID).collect(Collectors.toSet()), args[0]) || event.getMessage().getGuild().getOwnerLongID() == event.getMessage().getAuthor().getLongID()) {
                commandHandler.runCommand(event.getMessage(), args);
            } else {
                MessageSending.sendMessage(event.getMessage().getChannel(), "You don't have permissions!");
            }
        }
    }

    @Override
    public Collection<Command> getDiscordCommands() {
        Collection<Command> discordCommands = permissionManager.getDiscordCommands();
        discordCommands.addAll(commandHandler.getDiscordCommands());
        return discordCommands;
    }
}
