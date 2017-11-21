package usbbot.modules

import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newSingleThreadContext
import org.slf4j.LoggerFactory
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.handle.obj.IVoiceChannel
import usbbot.commands.DiscordCommands
import usbbot.commands.core.Command
import usbbot.util.MessageParsing
import usbbot.util.commands.AnnotationExtractor
import usbbot.util.commands.DiscordCommand
import usbbot.util.commands.DiscordSubCommand
import util.bufferedMoveToVoiceChannel
import util.sendError
import util.sendSuccess
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class Jail : DiscordCommands {
    override fun getDiscordCommands(): MutableCollection<Command> = AnnotationExtractor.getCommandList(this)
    private val logger = LoggerFactory.getLogger(Jail::class.java)
    private val coroutineContext = newSingleThreadContext("Jail Context")
    private val userJailMap = ConcurrentHashMap<Long, Pair<IVoiceChannel, Job>>()

    fun userMoved(user: IUser, channel: IVoiceChannel) {
        logger.debug("userMoved was called!")
        val userPair = userJailMap[user.longID]
        logger.debug("user pair is {}", userPair)
        if (userPair != null && userPair.first.guild.longID == channel.guild.longID) {
            logger.debug("trying to move a user!")
            user.bufferedMoveToVoiceChannel(userPair.first)
        }
    }

    @DiscordCommand("jail")
    fun jail(msg: IMessage, args: Array<String>) : Int {
        if (args.size >= 3 && args[1] == "pardon") return 0
        if (args.size < 4) {
            msg.channel.sendError("Not enough Arguments!")
            return -1
        }
        val user = msg.guild.getUserByID(MessageParsing.getUserID(args[1]))
        if (user == null) {
            msg.channel.sendError("That is not a valid user!")
            return -1
        }
        val channelId = args[2].toLongOrNull()
        if (channelId == null || msg.guild.getVoiceChannelByID(channelId) == null) {
            msg.channel.sendError("${args[2]} is not a valid Channel!}")
            return -1
        }
        val channel = msg.guild.getVoiceChannelByID(channelId)
        val time = args[3].toLongOrNull()
        if (time == null || time < 0) {
            msg.channel.sendError("${args[3]} is not a valid time!")
            return -1
        }
        //TODO make this work for the same user on multiple guilds...
        if (userJailMap[user.longID] != null) {
            msg.channel.sendError("${user.name} is already in jail!")
            return -1
        }

        userJailMap.put(user.longID, Pair(channel,
                launch(coroutineContext) {
                    delay(time, TimeUnit.SECONDS)
                    logger.debug("{} removed from jail! {}", user.longID, userJailMap.remove(user.longID))
                }))
        val voiceState = user.getVoiceStateForGuild(msg.guild)
        if (voiceState.channel != null) {
            user.moveToVoiceChannel(channel)
        }

        msg.channel.sendSuccess("Okay ${user.name} will not be able to leave ${channel.name} for the next $time seconds!")
        return -1
    }

    @DiscordSubCommand(parent = "jail", name = "pardon")
    fun jailPardon(msg: IMessage, args: Array<String>) {
        if (args.size < 3) {
            msg.channel.sendError("Not enough Arguments!")
            return
        }
        val userId = MessageParsing.getUserID(args[2])
        if (userId == -1L) {
            msg.channel.sendError("Not a valid user!")
            return
        }
        val pair = userJailMap[userId]
        if (pair == null) {
            msg.channel.sendError("That user is not currently in jail!")
            return
        }
        userJailMap.remove(userId)
        pair.second.cancel()
        msg.channel.sendSuccess("Okay that user is no longer in jail!")
    }


}