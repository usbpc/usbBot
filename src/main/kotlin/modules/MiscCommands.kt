package modules

import commands.DiscordCommands
import commands.core.Command
import org.slf4j.LoggerFactory
import sx.blah.discord.handle.obj.IMessage
import util.MessageSending
import util.commands.AnnotationExtractor
import util.commands.DiscordCommand

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
}