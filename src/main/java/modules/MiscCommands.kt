package modules

import commands.DiscordCommands
import commands.core.Command
import org.slf4j.LoggerFactory
import sx.blah.discord.handle.obj.IMessage
import util.MessageSending
import util.commands.AnnotationExtractor
import util.commands.DiscordCommand

class MiscCommands : DiscordCommands {
    var logger = LoggerFactory.getLogger(MiscCommands::class.java)
    override fun getDiscordCommands(): MutableCollection<Command> = AnnotationExtractor.getCommandList(this)


    @DiscordCommand("bulkdelete")
    fun bulkdelete(msg: IMessage, args: Array<String>) {
        val first = args[1].toLongOrNull()
        val second = args[2].toLongOrNull()
        if (first != null && second != null) {
            //TODO implement better sanity checks... messages in same channel, both exist and so on
            val history = if (first < second) {
                msg.channel.getMessageHistoryIn(second, first)
            } else {
                msg.channel.getMessageHistoryIn(first, second)
            }
            MessageSending.sendMessage(msg.channel, "Deleted " + history.size + " messages (hopefully)")
            history.bulkDelete()
        }
    }
}