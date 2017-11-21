package usbbot.modules.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import org.slf4j.LoggerFactory
import sx.blah.discord.handle.obj.IMessage
import usbbot.commands.DiscordCommands
import usbbot.commands.core.Command
import usbbot.util.commands.AnnotationExtractor
import usbbot.util.commands.DiscordCommand
import util.sendError
import util.sendSuccess

class Music : DiscordCommands {
    val logger = LoggerFactory.getLogger(Music::class.java)
    override fun getDiscordCommands(): MutableCollection<Command> = AnnotationExtractor.getCommandList(this)

    val guildMusicMap = HashMap<Long, GuildMusicManager>()
    val playerManager : AudioPlayerManager = DefaultAudioPlayerManager()

    init {
        logger.debug("Frame Buffer Duration is {}", playerManager.frameBufferDuration)
        playerManager.enableGcMonitoring()
        AudioSourceManagers.registerRemoteSources(playerManager)
    }

    @DiscordCommand("leave")
    fun leave(msg: IMessage, args: Array<String>) {
        val musicManager = guildMusicMap[msg.guild.longID]
        if (musicManager != null) {
            guildMusicMap.remove(msg.guild.longID)
            musicManager.scheduler.clear()
            musicManager.player.destroy()
        }
        msg.client.ourUser.getVoiceStateForGuild(msg.guild).channel?.leave()
    }

    @DiscordCommand("pause")
    fun pause(msg: IMessage, args: Array<String>) {
        guildMusicMap[msg.guild.longID]?.player?.isPaused = true
    }

    @DiscordCommand("resume")
    fun resume(msg: IMessage, args: Array<String>) {
        guildMusicMap[msg.guild.longID]?.player?.isPaused = false
    }

    @DiscordCommand("queue")
    fun queue(msg: IMessage, args: Array<String>) {
        val musicManager = guildMusicMap[msg.guild.longID]
        if (musicManager == null) {
            msg.channel.sendError("There isn't any music playing!")
        } else {
            val builder = StringBuilder()
            builder.append("Current queue: \n")
            musicManager.scheduler.queue.forEach {
                builder.append(it.info.title).append('\n')
            }
            msg.channel.sendSuccess(builder.toString())
        }
    }

    @DiscordCommand("play")
    fun play(msg: IMessage, args: Array<String>) {

        if (msg.client.ourUser.getVoiceStateForGuild(msg.guild).channel == null) {
            val authorChannel = msg.author.getVoiceStateForGuild(msg.guild).channel
            if (authorChannel == null) {
                msg.channel.sendError("You are not in a voice channel!")
                return
            } else {
                authorChannel.join()
            }
        }

        val guildMusicManager = guildMusicMap.getOrPut(msg.guild.longID) {
            val musicManager = GuildMusicManager(playerManager, msg.guild)
            msg.guild.audioManager
            msg.guild.audioManager.audioProvider = musicManager.audioProvider
            musicManager
        }

        playerManager.loadItemOrdered(guildMusicManager, args[1], MyAudioLoadResultHandler(msg.channel, guildMusicManager, args[1]))
    }
}

