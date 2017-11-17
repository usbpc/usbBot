package usbbot.modules

import usbbot.commands.DiscordCommands
import usbbot.commands.core.Command
import sx.blah.discord.handle.obj.IMessage
import usbbot.config.getDBTextCommand
import usbbot.config.getHelptext
import usbbot.util.MessageSending
import usbbot.util.commands.AnnotationExtractor
import usbbot.util.commands.DiscordCommand

class HelpCommand : DiscordCommands {
    override fun getDiscordCommands(): MutableCollection<Command> {
        return AnnotationExtractor.getCommandList(this)
    }

    @DiscordCommand("help")
    fun help(msg: IMessage, args: Array<String>) {
        if (args.size > 1) {
            val helpText = getHelptext(args[1])
            if (helpText != null) {
                MessageSending.sendMessage(msg.channel, "Syntax for command `" + args[1] + "`\n```\n" + helpText + "```")
            } else {
                val dbTextCommand = getDBTextCommand(msg.guild.longID, args[1])
                if ( dbTextCommand != null) {
                    MessageSending.sendMessage(msg.channel, "`" + args[1] +
                            "` is just a simple text command that answers with:\n" + "```" + dbTextCommand.text + "```")
                } else {
                    MessageSending.sendMessage(msg.channel, "`" + args[1] + "` is not a DBCommand.")
                }
            }
        } else {
            MessageSending.sendMessage(msg.channel, "To see all available usbbot.commands use the `list` command.\n" +
                    "For more information about a command use `help <command>`")
        }
    }
}