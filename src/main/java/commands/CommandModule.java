package commands;

import commands.annotations.AnnotationRegister;
import commands.core.Command;
import commands.core.CommandHandler;
import commands.security.PermissionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.Collection;
public class CommandModule {
    private CommandHandler commandHandler;
    private PermissionManager permissionManager;
    private AnnotationRegister annotationRegister;
    private Logger logger = LoggerFactory.getLogger(CommandModule.class);
    public CommandModule() {
        commandHandler = new CommandHandler();
        permissionManager = new PermissionManager(commandHandler);
        annotationRegister = new AnnotationRegister(permissionManager);

        registerCommandsFromObject(commandHandler);
        registerCommandsFromObject(permissionManager);
    }

    public void registerCommand(Command command) {
        commandHandler.registerCommand(command);
    }

    public void registerCommands(Collection<Command> commands) {
        commands.forEach(this::registerCommand);
    }
    //TODO removing commands dosen't remove their permissions
    public void unregisterCommand(String name) {
        commandHandler.unregisterCommand(name);
    }

    public void unregisterCommands(Collection<String> commands) {
        commands.forEach(this::unregisterCommand);
    }

    public void registerCommandsFromObject(Object obj) {
        registerCommands(annotationRegister.getCommandList(obj));
    }

    /*public void addRoleToCommandPermissions(String commandName, Long roleID) {
        if (getCommand(commandName) == null) throw new IllegalArgumentException(commandName + " is not a valid command");
        permissionManager.addRoleToPermission(commandName, roleID);
    }*/

    public void registerCommandFromObjects(Collection<Object> objs) {
        objs.forEach(this::registerCommandsFromObject);
    }

    public Collection<Command> getAllCommands() {
        return commandHandler.getAllCommands();
    }

    public Command getCommand(String uuid) {
        return commandHandler.getCommandByName(uuid);
    }

    @EventSubscriber
    //TODO check for permissions here so permissions don't need to know about the commands and vice versa
    public void runCommand(MessageReceivedEvent event) {

        logger.debug("#{} @{} : {}", event.getChannel().getName(), event.getAuthor().getName(), event.getMessage().getContent());
        //System.out.printf("[CommandModule] #%s @%s : %s\r\n", event.getChannel().getName(), event.getAuthor().getName(), event.getMessage().getContent());
        if (commandHandler.isCommand(event.getMessage().getContent())) {
            String[] args = commandHandler.getArguments(event.getMessage().getContent());
            if (permissionManager.hasPermission(event.getMessage(), args[0]) || event.getMessage().getGuild().getOwnerLongID() == event.getMessage().getAuthor().getLongID()) {
                commandHandler.runCommand(event.getMessage(), args);
            }
        }
    }
}
