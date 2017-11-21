package usbbot.modules.music

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import sx.blah.discord.handle.obj.IChannel
import util.sendError
import util.sendSuccess

class MyData(val requestChannel: IChannel)

class MyAudioLoadResultHandler(val channel: IChannel, val musicManager: GuildMusicManager, val searchParam: String) : AudioLoadResultHandler {

    override fun loadFailed(exception: FriendlyException) {
        if (exception.severity != FriendlyException.Severity.FAULT) {
            channel.sendError("Sorry, I can't play that Video:\n```${exception.message}```")
        } else {
            channel.sendError("Something went horribly Wrong:\n```${exception.message}```")
        }
    }

    override fun trackLoaded(track: AudioTrack) {
        track.userData = MyData(channel)
        musicManager.scheduler.queue(track)
        channel.sendSuccess("Added ${track.info.title} to the queue!")
    }

    override fun noMatches() {
        channel.sendError("Couldn't find anything for $searchParam")
    }

    override fun playlistLoaded(playlist: AudioPlaylist) {
        var firstTrack = playlist.selectedTrack

        if (firstTrack == null) {
            firstTrack = playlist.tracks[0]
        }

        channel.sendSuccess("Added ${firstTrack.info.title} (first track of playlist ${playlist.name})")

        firstTrack.userData = MyData(channel)
        musicManager.scheduler.queue(firstTrack)
    }

}