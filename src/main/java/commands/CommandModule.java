package commands;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.Collection;
public class CommandModule {
    public CommandModule(/*config*/) {

    }

    public void registerCommand(Command command) {

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

    }
}
