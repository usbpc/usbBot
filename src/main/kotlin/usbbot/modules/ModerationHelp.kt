package usbbot.modules

import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ClosedSendChannelException
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newSingleThreadContext
import kotlinx.coroutines.experimental.runBlocking
import org.slf4j.LoggerFactory
import sx.blah.discord.handle.impl.obj.VoiceState
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IVoiceState
import sx.blah.discord.handle.obj.Permissions
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.MessageHistory
import sx.blah.discord.util.PermissionUtils
import sx.blah.discord.util.RequestBuffer
import usbbot.commands.DiscordCommands
import usbbot.commands.core.Command
import usbbot.util.MessageParsing
import usbbot.util.MessageSending
import usbbot.util.commands.AnnotationExtractor
import usbbot.util.commands.DiscordCommand
import usbbot.util.commands.DiscordSubCommand
import util.*
import java.io.ByteArrayInputStream
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

class ModerationHelp : DiscordCommands {
    val logger = LoggerFactory.getLogger(ModerationHelp::class.java)

    override fun getDiscordCommands(): MutableCollection<Command> = AnnotationExtractor.getCommandList(this)

    @DiscordCommand("massmove")
    fun massmove(msg: IMessage, args: Array<String>) {
        if (args.size < 2) {
            msg.channel.sendError("Invalid Syntax")
            return
        }

        val location = args[1].toLongOrNull()
        if (location == null) {
            msg.channel.sendError("${args[1]} is not a a valid number")
            return
        }

        val goalChannel = msg.guild.getVoiceChannelByID(location)
        if (goalChannel == null) {
            msg.channel.sendError("${args[1]} does not represented a valid voice channel")
            return
        }

        if (!goalChannel.checkPermissions(msg.author, Permissions.VOICE_MOVE_MEMBERS)) {
            msg.channel.sendError("You do not have move permissions for the channel that you try to move to!")
            return
        }

        val voiceState : IVoiceState? = msg.author.getVoiceStateForGuild(msg.guild)
        if (voiceState == null) {
            msg.channel.sendError("You are not currently in any voice channel!")
            return
        }

        if (!goalChannel.checkOurPermissionsOrSendError(msg.channel, Permissions.VOICE_MOVE_MEMBERS) ||
                !voiceState.channel.checkOurPermissionsOrSendError(msg.channel, Permissions.VOICE_MOVE_MEMBERS))
            return

        val message = msg.channel.sendProcessing("Will now move everyone...")

        voiceState.channel.connectedUsers.forEach {
            RequestBuffer.request {
                it.moveToVoiceChannel(goalChannel)
            }
        }

        message.updateSuccess("Everyone was moved!")
    }

    @DiscordCommand("getroleids")
    fun getroleids(msg: IMessage, vararg args: String) {
        val builder = StringBuilder()
        msg.guild.roles.forEach { role -> builder.append(role.name).append(": ").append(role.longID).append('\n') }
        msg.channel.sendSuccess("There are the IDs I found: ```" + builder.toString() + "```")
    }

    @DiscordCommand("getuserids")
    fun getuserids(msg: IMessage, vararg args: String) {

        val builder = StringBuilder()
        msg.guild.users.forEach { user -> builder.append(user.name).append(": ").append(user.longID).append("\r\n") }
        builder.deleteCharAt(builder.length - 1)
        builder.deleteCharAt(builder.length - 1)
        //MessageBuilder msgBuilder = new MessageBuilder(msg.getClient());

        //msgBuilder.withChannel(msg.getChannel()).withFile(new ByteArrayInputStream(builder.toString().getBytes()), "test.txt").build();

        //msg.getChannel().sendFile("List of all users: ", new ByteArrayInputStream(builder.toString().getBytes()), "users.txt");
        MessageSending.sendFile(msg.channel, "List of all users: ", ByteArrayInputStream(builder.toString().toByteArray()), "users.txt")

    }
}