package commands;

import config.ConfigObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.Collection;
public class CommandModule {
    CommandHandler commandHandler;
    PermissionManager permissionManager;
    CommandRegisterHelper commandRegisterHelper;
    private Logger logger = LoggerFactory.getLogger(CommandModule.class);
    public CommandModule() {
        commandHandler = new CommandHandler();
        permissionManager = new PermissionManager(commandHandler);
        commandRegisterHelper = new CommandRegisterHelper(permissionManager);

        registerCommandsFromObject(commandHandler);
        registerCommandsFromObject(permissionManager);
    }

    public void registerCommand(Command command) {
        commandHandler.registerCommand(command);
    }

    public void registerCommands(Collection<Command> commands) {
        commands.forEach(this::registerCommand);
    }

    public void unregisterCommand(String name) {
        commandHandler.unregisterCommand(name);
    }

    public void unregisterCommands(Collection<String> commands) {
        commands.forEach(this::unregisterCommand);
    }

    public void registerCommandsFromObject(Object obj) {
        registerCommands(commandRegisterHelper.getCommandList(obj));
    }

    public void addRoleToCommandPermissions(String commandName, Long roleID) {
        if (getCommand(commandName) == null) throw new IllegalArgumentException(commandName + " is not a valid command");
        permissionManager.addRoleToPermission(commandName, roleID);
    }

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
    public void runCommand(MessageReceivedEvent event) {

        logger.debug("#{} @{} : {}", event.getChannel().getName(), event.getAuthor().getName(), event.getMessage().getContent());
        //System.out.printf("[CommandModule] #%s @%s : %s\r\n", event.getChannel().getName(), event.getAuthor().getName(), event.getMessage().getContent());
        commandHandler.runCommand(event);
    }
}
