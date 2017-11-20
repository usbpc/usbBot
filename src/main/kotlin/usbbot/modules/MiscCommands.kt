package usbbot.modules

import at.mukprojects.giphy4j.Giphy
import at.mukprojects.giphy4j.exception.GiphyException
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import usbbot.commands.DiscordCommands
import usbbot.commands.core.Command
import usbbot.main.UsbBot
import org.slf4j.LoggerFactory
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.Permissions
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.RequestBuffer
import usbbot.util.MessageParsing
import usbbot.util.MessageSending
import usbbot.util.commands.AnnotationExtractor
import usbbot.util.commands.DiscordCommand
import util.*
import java.awt.Color
import java.util.concurrent.TimeUnit

class MiscCommands : DiscordCommands {
    val giphy = Giphy(UsbBot.getProperty("giphy"))
    val logger = LoggerFactory.getLogger(MiscCommands::class.java)

    override fun getDiscordCommands(): MutableCollection<Command> = AnnotationExtractor.getCommandList(this)

    @DiscordCommand("getavatarlink")
    fun getavatarlink(msg: IMessage, vararg args: String) {
        if (args.size < 2) {
            msg.channel.sendSuccess(msg.author.avatarURL)
        } else if (msg.mentions.size  == 1) {
            msg.channel.sendSuccess(msg.mentions.first().avatarURL)
        } else {
            msg.channel.sendError("Invalid syntax")
        }
    }

    @DiscordCommand("cat")
    fun cat(msg: IMessage, args: Array<String>) {
        val messageFuture = msg.channel.sendProcessing("Loading...")
        val embed = EmbedBuilder()
                .withImage(giphy.searchRandom("cute cat").data.imageOriginalUrl)
                .withColor(Color.GREEN)
                //.withTitle("A cute cat:")
                //.withAuthorName(msg.client.ourUser.getDisplayName(msg.guild))
                //.withAuthorIcon(msg.client.ourUser.avatarURL)
                .withFooterText("Powered By GIPHY")
        val message = messageFuture.get()
        RequestBuffer.request { message.edit(embed.build()) }
    }

    @DiscordCommand("gif")
    fun giphy(msg: IMessage, args: Array<String>) {
        if (args.size < 2) {
            msg.channel.sendError("Please specify a search term...")
            return
        }
        val messageFuture = msg.channel.sendProcessing("Loading...")
        val builder = StringBuilder()
        args.drop(1).forEach { builder.append(it).append(" ") }
        try {
            val embed = EmbedBuilder()
                    .withImage(giphy.searchRandom(builder.toString()).data.imageOriginalUrl)
                    .withColor(Color.GREEN)
                    //.withTitle("A cute cat:")
                    //.withAuthorName(msg.client.ourUser.getDisplayName(msg.guild))
                    //.withAuthorIcon(msg.client.ourUser.avatarURL)
                    .withFooterText("Powered By GIPHY")
            messageFuture.updateSuccess(embed.build())
        } catch (ex: GiphyException) {
            messageFuture.updateError("Could not find a gif for `$builder`")
        }
    }

    @DiscordCommand("hug")
    fun hug(msg: IMessage, args: Array<String>) {
        if (msg.channel.checkOurPermissions(Permissions.MANAGE_MESSAGES)) {
            msg.bufferedDelete()
        }
        val userID = if (args.size > 1) {
            var tmp = MessageParsing.getUserID(args[1])
            if (tmp == -1L) {
                msg.author.longID
            } else {
                tmp
            }
        } else {
            msg.author.longID
        }
        msg.channel.sendSuccess("*hugs <@$userID>*")
    }

    @DiscordCommand("spam")
    fun spam(msg: IMessage, args: Array<String>) = launch {
        var msgCount = args[1].toInt()
        while (msgCount-- > 0) {
            delay(1500)
            MessageSending.sendMessage(msg.channel, msgCount.toString()).get()
        }
    }
}