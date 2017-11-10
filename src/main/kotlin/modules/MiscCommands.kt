package modules

import commands.DiscordCommands
import commands.core.Command
import org.slf4j.LoggerFactory
import sx.blah.discord.handle.obj.IMessage
import util.MessageParsing
import util.MessageSending
import util.commands.AnnotationExtractor
import util.commands.DiscordCommand
import java.io.ByteArrayInputStream

class MiscCommands : DiscordCommands {
    val logger = LoggerFactory.getLogger(MiscCommands::class.java)
    override fun getDiscordCommands(): MutableCollection<Command> = AnnotationExtractor.getCommandList(this)

    //TODO: delete last x messages
    //TODO: delete last x messages by user
    @DiscordCommand("bulkdelete")
    fun bulkdelete(msg: IMessage, args: Array<String>) {
        val first = args[1].toLongOrNull()
        val second = args[2].toLongOrNull()
        if (first != null && second != null) {
            val firstMsg : IMessage? = msg.channel.getMessageByID(first)
            val secondMsg : IMessage? = msg.channel.getMessageByID(second)

            if (firstMsg == null || secondMsg == null) {
                MessageSending.sendMessage(msg.channel, "Both messages need to be in the same channel, and it has to be the channel where you execute this command!")
            } else {
                val history = if (firstMsg.timestamp.isAfter(secondMsg.timestamp)) {
                    msg.channel.getMessageHistoryIn(first, second)
                } else {
                    msg.channel.getMessageHistoryIn(second, first)
                }
                MessageSending.sendMessage(msg.channel, "Deleted " + history.size + " messages (hopefully)")
                history.bulkDelete()
            }
        }
    }

    @DiscordCommand("getroleids")
    fun getroleids(msg: IMessage, vararg args: String) {
        val builder = StringBuilder()
        msg.guild.roles.forEach { role -> builder.append(role.name).append(": ").append(role.longID).append('\n') }
        MessageSending.sendMessage(msg.channel, "There are the IDs I found: ```" + builder.toString() + "```")
    }

    @DiscordCommand("getavatarlink")
    fun getavatarlink(msg: IMessage, vararg args: String) {
        MessageSending.sendMessage(msg.channel, msg.client.getUserByID(MessageParsing.getUserID(args[1])).avatarURL)
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