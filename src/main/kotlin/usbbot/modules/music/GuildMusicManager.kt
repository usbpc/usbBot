package usbbot.modules.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import sx.blah.discord.handle.obj.IGuild

/**
 * Holder for both the player and a track scheduler for one guild.
 */
class GuildMusicManager
/**
 * Creates a player and a track scheduler.
 * @param manager Audio player manager to use for creating the player.
 */
(manager: AudioPlayerManager, guild: IGuild) {
    /**
     * Audio player for the guild.
     */
    val player: AudioPlayer = manager.createPlayer()
    /**
     * Track scheduler for the player.
     */
    val scheduler: TrackScheduler

    /**
     * @return Wrapper around AudioPlayer to use it as an AudioSendHandler.
     */
    val audioProvider: AudioProvider
        get() = AudioProvider(player)

    init {
        scheduler = TrackScheduler(player, guild)
        player.addListener(scheduler)
    }
}