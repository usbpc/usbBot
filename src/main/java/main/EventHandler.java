package main;

import commands.CommandModule;
import modules.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.GuildCreateEvent;
import sx.blah.discord.handle.impl.events.guild.GuildLeaveEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelJoinEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelLeaveEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelMoveEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * @author usbpc
 * @since 2017-05-18
 */
public class EventHandler {
    private UsbBot usbBot;
    private Map<Long, CommandModule> commandModuleMap = new HashMap<>();
    public EventHandler(UsbBot usbBot) {
        this.usbBot = usbBot;
    }
    private static Logger logger = LoggerFactory.getLogger(EventHandler.class);
    @EventSubscriber
    public void onUserVoiceChannelJoinEvent(UserVoiceChannelJoinEvent event) {
        logger.debug("Someone joined: {}", event);
        MoreVoiceChannelsKt.someoneJoined(event);
    }

    @EventSubscriber
    public void onUserVoiceChannelMoveEvent(UserVoiceChannelMoveEvent event) {
        logger.debug("Someone moved: {}", event);
        MoreVoiceChannelsKt.someoneMoved(event);
    }

    @EventSubscriber
    public void onUserVoiceChannelLeaveEvent(UserVoiceChannelLeaveEvent event) {
        logger.debug("Someone left: {}", event);
        MoreVoiceChannelsKt.someoneLeft(event);
    }

    @EventSubscriber
    public void onGuildCreateEvent(GuildCreateEvent event) {
        logger.debug("I'm connected to {}", event.getGuild().getName());
        CommandModule commandModule = new CommandModule(event.getGuild().getLongID());
        commandModule.registerCommands(commandModule);
        //TODO limit this to only one guild or something... I don't know, but it contains the command to shut my bot down, so I need to be carefull if I ever let my bot onto other discord guilds
        //commandModule.registerCommands(new TestCommands());
        commandModule.registerCommands(new SimpleTextResponses(commandModule, event.getGuild().getLongID()));
        commandModule.registerCommands(new HelpCommand());
        commandModule.registerCommands(new MiscCommands());
        commandModule.registerCommands(new MoreVoiceChannel());
        commandModuleMap.put(event.getGuild().getLongID(), commandModule);
    }

    @EventSubscriber
    public void onMessageReceivedEvent(MessageReceivedEvent event) {
        if (event.getChannel().isPrivate()) {
            logger.debug("PN:#{}({}):@{}({}): {}",
                    event.getChannel().getLongID(),
                    event.getAuthor().getDisplayName(event.getGuild()),
                    event.getAuthor().getLongID(),
                    event.getMessage());
            event.getChannel().sendMessage("Sorry, but I currently don't support any commands in Private messages");
        } else {
            logger.debug("{}({}):#{}({}):@{}({}): {}",
                    event.getGuild().getName(),
                    event.getGuild().getLongID(),
                    event.getChannel().getName(),
                    event.getChannel().getLongID(),
                    event.getAuthor().getDisplayName(event.getGuild()),
                    event.getAuthor().getLongID(),
                    event.getMessage());
            commandModuleMap.get(event.getGuild().getLongID()).runCommand(event);
        }
    }

    @EventSubscriber
    public void onGuildLeaveEvent(GuildLeaveEvent event) {

    }
}
