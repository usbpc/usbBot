package usbbot.modules

import sx.blah.discord.handle.obj.IMessage
import usbbot.commands.DiscordCommands
import usbbot.commands.core.Command
import usbbot.config.setGuildPrefix
import usbbot.util.MessageSending
import usbbot.util.commands.AnnotationExtractor
import usbbot.util.commands.DiscordCommand
import util.sendError
import util.sendSuccess

class SystemCommands : DiscordCommands{
    override fun getDiscordCommands(): MutableCollection<Command> = AnnotationExtractor.getCommandList(this)

    @DiscordCommand("prefix")
    fun prefix(msg: IMessage, vararg args: String) {
        if (args.size < 2) {
            msg.channel.sendError("You need to specify a new prefix!")
        } else {
            setGuildPrefix(msg.guild.longID, args[1])
            msg.channel.sendSuccess("The Command prefix is now " + args[1])
        }
    }
}