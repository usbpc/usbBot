package usbbot.modules.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame
import sx.blah.discord.handle.audio.AudioEncodingType
import sx.blah.discord.handle.audio.IAudioProvider

/**
 * This is a wrapper around AudioPlayer which makes it behave as an IAudioProvider for D4J. As D4J calls canProvide
 * before every call to provide(), we pull the frame in canProvide() and use the frame we already pulled in
 * provide().
 */
class AudioProvider
/**
 * @param audioPlayer Audio player to wrap.
 */
(private val audioPlayer: AudioPlayer) : IAudioProvider {
    private var lastFrame: AudioFrame? = null

    override fun isReady(): Boolean {
        if (lastFrame == null) {
            lastFrame = audioPlayer.provide()
        }

        return lastFrame != null
    }

    override fun provide(): ByteArray? {
        if (lastFrame == null) {
            lastFrame = audioPlayer.provide()
        }

        val data = if (lastFrame != null) lastFrame?.data else null
        lastFrame = null

        return data
    }

    override fun getChannels(): Int = 2

    override fun getAudioEncodingType(): AudioEncodingType = AudioEncodingType.OPUS
}