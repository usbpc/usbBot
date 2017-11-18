package usbbot.modules

import sx.blah.discord.handle.obj.IMessage
import usbbot.commands.DiscordCommands
import usbbot.commands.core.Command
import usbbot.config.setGuildPrefix
import usbbot.util.MessageSending
import usbbot.util.commands.AnnotationExtractor
import usbbot.util.commands.DiscordCommand

class SystemCommands : DiscordCommands{
    override fun getDiscordCommands(): MutableCollection<Command> = AnnotationExtractor.getCommandList(this)

    @DiscordCommand("prefix")
    fun prefix(msg: IMessage, vararg args: String) {
        if (args.size < 2) MessageSending.sendMessage(msg.channel, "You need to specify a new prefix!")
        else {
            setGuildPrefix(msg.guild.longID, args[1])
            MessageSending.sendMessage(msg.channel, "The DBCommand prefix is now " + args[1])
        }
    }
}