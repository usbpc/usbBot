package usbbot.modules.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import sx.blah.discord.handle.obj.IGuild
import util.sendError
import util.sendSuccess
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

/**
 * This class schedules tracks for the audio player. It contains the queue of tracks.
 */
class TrackScheduler
/**
 * @param player The audio player this scheduler uses
 */
(private val player: AudioPlayer, val guild: IGuild) : AudioEventAdapter() {
    val queue: BlockingQueue<AudioTrack>

    init {
        this.queue = LinkedBlockingQueue()
    }


    fun clear() {
        queue.clear()
    }

    /**
     * Add the next track to queue or play right away if nothing is in the queue.
     *
     * @param track The track to play or add to queue.
     */
    fun queue(track: AudioTrack) {
        // Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
        // something is playing, it returns false and does nothing. In that case the player was already playing so this
        // track goes to the queue instead.
        if (!player.startTrack(track, true)) {
            queue.offer(track)
        }
    }

    /**
     * Start the next track, stopping the current one if it is playing.
     */
    fun nextTrack() {
        // Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
        // giving null to startTrack, which is a valid argument and will simply stop the player.
        if (!queue.isEmpty()) {
            player.startTrack(queue.poll(), false)
        } else {
            guild.client.ourUser.getVoiceStateForGuild(guild).channel.leave()
        }
    }

    override fun onTrackStart(player: AudioPlayer, track: AudioTrack) {
        (track.userData as MyData).requestChannel.sendSuccess("Now playing ${track.info.title}")
    }

    override fun onTrackStuck(player: AudioPlayer, track: AudioTrack, thresholdMs: Long) {
        nextTrack()
        (track.userData as MyData).requestChannel.sendError("The Music got stuck, skipping to next song...")
    }

    override fun onTrackException(player: AudioPlayer, track: AudioTrack, exception: FriendlyException) {
        nextTrack()
        (track.userData as MyData).requestChannel.sendError("Something went wrong while playing ${track.info.title}:\n```${exception.message}```\nWill skip to the next song.")
    }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
        if (endReason.mayStartNext) {
            nextTrack()
        }
    }
}