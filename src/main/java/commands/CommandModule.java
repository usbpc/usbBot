package commands;

import config.ConfigObject;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.Collection;
public class CommandModule {
    CommandHandler commandHandler;
    PermissionManager permissionManager;
    CommandRegisterHelper commandRegisterHelper;
    public CommandModule(ConfigObject commandConfig) {
        commandHandler = new CommandHandler();
        permissionManager = new PermissionManager(commandHandler, commandConfig);
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

    public void unregisterCommand(Command command) {

    }

    public void unregisterCommands(Collection<Command> commands) {
        commands.forEach(this::unregisterCommand);
    }

    public void registerCommandsFromObject(Object obj) {
        registerCommands(commandRegisterHelper.getCommandList(obj));
    }

    public void registerCommandFromObjects(Collection<Object> objs) {
        objs.forEach(this::registerCommandsFromObject);
    }

    public Collection<Command> getAllCommands() {
        return null;
    }

    public Collection<Command> getCommand(String uuid) {
        return null;
    }

    @EventSubscriber
    public void runCommand(MessageReceivedEvent event) {
        commandHandler.runCommand(event);
    }
}
