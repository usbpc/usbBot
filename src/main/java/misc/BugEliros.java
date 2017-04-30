package misc;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.TypingEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelJoinEvent;
import sx.blah.discord.handle.impl.events.user.PresenceUpdateEvent;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.StatusType;

/**
 * Created by kjh on 29.04.2017.
 */
public class BugEliros {
    @EventSubscriber
    private void onMessageReceivedEvent(MessageReceivedEvent event) {
        if (event.getAuthor().getLongID() == 105306394130968576L) {
            event.getGuild().getUserByID(104214100422180864L).getOrCreatePMChannel().sendMessage("Eliros just wrote a message in " + event.getChannel().getName());
        }
    }

    @EventSubscriber
    private void onTypingEvent(TypingEvent event) {
        if (event.getUser().getLongID() == 105306394130968576L) {
            event.getGuild().getUserByID(104214100422180864L).getOrCreatePMChannel().sendMessage("Eliros just started typing something in " + event.getChannel().getName());
        }
    }

    @EventSubscriber
    private void onPresenceUpdateEvent(PresenceUpdateEvent event) {
        if (event.getUser().getLongID() == 105306394130968576L) {
            event.getClient().getUserByID(104214100422180864L).getOrCreatePMChannel().sendMessage("Eliros just changed his status from " + event.getOldPresence().getStatus().toString() + " to " + event.getNewPresence().getStatus().toString());
        }
    }

    @EventSubscriber
    private void onUserVoiceChannelJoinEvent(UserVoiceChannelJoinEvent event) {
        if (event.getUser().getLongID() == 105306394130968576L) {
            event.getClient().getUserByID(104214100422180864L).getOrCreatePMChannel().sendMessage("Eliros just joined a voice channel " + event.getVoiceChannel().getName());
        }
    }
}
