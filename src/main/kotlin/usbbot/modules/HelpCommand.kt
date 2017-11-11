package usbbot.modules

import usbbot.commands.DiscordCommands
import usbbot.commands.core.Command
import usbbot.config.MiscSQLCommand
import usbbot.config.SimpleTextCommandsSQL
import sx.blah.discord.handle.obj.IMessage
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
            val helpText = MiscSQLCommand.getHelpText(args[1])
            if (helpText != null) {
                MessageSending.sendMessage(msg.channel, "Syntax for command `" + args[1] + "`\n```" + helpText + "```")
            } else {
                val responseText = SimpleTextCommandsSQL.getCommandText(msg.guild.longID, args[1]);
                if ( responseText != null) {
                    MessageSending.sendMessage(msg.channel, "`" + args[1] +
                            "` is just a simple text command that answers with:\n" + "```" + responseText + "```")
                } else {
                    MessageSending.sendMessage(msg.channel, "`" + args[1] + "` is not a Command.")
                }
            }
        } else {
            MessageSending.sendMessage(msg.channel, "To see all available usbbot.commands use the `list` command.\n" +
                    "For more information about a command use `help <command>`")
        }
    }
}