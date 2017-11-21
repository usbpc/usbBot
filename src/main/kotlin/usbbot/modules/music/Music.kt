package usbbot.modules.music

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import javafx.beans.DefaultProperty
import org.slf4j.LoggerFactory
import sx.blah.discord.handle.audio.impl.DefaultProvider
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IUser
import usbbot.commands.DiscordCommands
import usbbot.commands.core.Command
import usbbot.util.commands.AnnotationExtractor
import usbbot.util.commands.DiscordCommand
import util.getLogger
import util.sendError
import util.sendSuccess
import java.net.MalformedURLException
import java.net.URI
import java.net.URL
import java.util.regex.Matcher
import java.util.regex.Pattern

class MyData(val requestedBy: Long, var id: Int = -1, val forGuild: Long, val youtubeId: String)

class Music : DiscordCommands {
    val logger = LoggerFactory.getLogger(Music::class.java)
    override fun getDiscordCommands(): MutableCollection<Command> = AnnotationExtractor.getCommandList(this)
    companion object {
        val ytbeRegex = Pattern.compile("/([0-9a-zA-Z_\\-]{11})")
        val ytcomRegex = Pattern.compile("v=([0-9a-zA-Z_\\-]{11})")
        val guildMusicMap = HashMap<Long, GuildMusicManager>()
        val playerManager : AudioPlayerManager = DefaultAudioPlayerManager()
    }
    init {
        logger.debug("Frame Buffer Duration is {}", playerManager.frameBufferDuration)
        playerManager.enableGcMonitoring()
        AudioSourceManagers.registerRemoteSources(playerManager)
    }

    fun getYouTubeURL(maybeUrl: String) : String? {
        val url = try {
            URL(maybeUrl)
        } catch (e: MalformedURLException) {
            if (e.message?.startsWith("no protocol") == true) {
                try {
                    URL("https://${maybeUrl}")
                } catch (e: Exception) {
                    return null
                }
            } else {
                return null
            }
        }

        return if (url.host.matches("(?:www\\.)?youtube\\.com".toRegex()) && url.path == "/watch") {
            val matcher = ytcomRegex.matcher(url.query)
            if (matcher.find()) {
                matcher.group(1)
            } else {
                null
            }
        } else if (url.host.matches("(?:www\\.)?youtu\\.be".toRegex())) {
            val matcher = ytbeRegex.matcher(url.path)
            if (matcher.find()) {
                matcher.group(1)
            } else {
                null
            }
        } else {
            null
        }
    }

    @DiscordCommand("play")
    fun play(msg: IMessage, args: Array<String>) {
        if (args.size < 2) {
            msg.channel.sendError("Not enough Arguments!")
            return
        }

        val url : String? = getYouTubeURL(args[1])
        if (url == null) {
            msg.channel.sendError("${args[1]} is not a valid youtube URL")
            return
        }

        val guildMusicManager = guildMusicMap.getOrPut(msg.guild.longID) {
            GuildMusicManager(playerManager, msg.guild)
        }

        logger.debug("Audio provider is ${msg.guild.audioManager.audioProvider}")

        if (msg.guild.audioManager.audioProvider is DefaultProvider) {
            msg.guild.audioManager.audioProvider = guildMusicManager.audioProvider
        }

        if (msg.client.ourUser.getVoiceStateForGuild(msg.guild).channel == null) {
            msg.author.getVoiceStateForGuild(msg.guild).channel.join()
        }
        val channel = msg.channel
        playerManager.loadItemOrdered(guildMusicManager, url, object: AudioLoadResultHandler {
            override fun loadFailed(exception: FriendlyException) {
                if (exception.severity != FriendlyException.Severity.FAULT) {
                    logger.debug("Something went wrong while trying to get that video", exception)
                    channel.sendError("Sorry, I can't play that Song:\n```${exception.message}```")
                } else {
                    channel.sendError("Something went horribly wrong:\n```${exception.message}```")
                }
            }

            override fun trackLoaded(track: AudioTrack) {
                track.userData = MyData(msg.author.longID, -1 ,msg.guild.longID, url)
                guildMusicManager.scheduler.queue(track)
                channel.sendSuccess("Added `${track.info.title}` to the queue!")
            }

            override fun noMatches() {
                channel.sendError("Couldn't find anything for ${args[1]}")
            }

            override fun playlistLoaded(playlist: AudioPlaylist) {
                throw IllegalStateException("There should never be a playlist requested! (╯°□°）╯︵ ┻━┻")
            }
        })

    }

    @DiscordCommand("playnext")
    fun playnext(msg: IMessage, args: Array<String>) {

    }

    @DiscordCommand("skip")
    fun skip(msg: IMessage, args: Array<String>) {
        guildMusicMap[msg.guild.longID]?.scheduler?.nextTrack()
    }

    @DiscordCommand("queue")
    fun queue(msg: IMessage, args: Array<String>) {

    }


    @DiscordCommand("leave")
    fun leave(msg: IMessage, args: Array<String>) {
        msg.client.ourUser.getVoiceStateForGuild(msg.guild)?.channel?.leave()
    }

    @DiscordCommand("pause")
    fun pause(msg: IMessage, args: Array<String>) {

    }

    @DiscordCommand("resume")
    fun resume(msg: IMessage, args: Array<String>) {

    }

}

