package modules;

import sx.blah.discord.handle.obj.IVoiceChannel;

/**
 * @author usbpc
 * @since 2017-07-18
 */
public class UnlimitedVoiceRooms {
    //Quatschen: 272724606802984960
    //TODO finish this
    public static void someoneEntered(IVoiceChannel channel) {
        if (channel.getLongID() == 272724606802984960L) {
            IVoiceChannel createdVoiceChannel = channel.getGuild().createVoiceChannel("Quatschen [2]");
            createdVoiceChannel.edit(createdVoiceChannel.getName(), channel.getPosition(), channel.getBitrate(), channel.getUserLimit());
        }
    }
}
