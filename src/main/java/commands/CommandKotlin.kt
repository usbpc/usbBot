package commands

import commands.core.Command
import commands.security.PermissionManager
import modules.SimpleTextResponses
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import sx.blah.discord.handle.obj.IMessage

class CommandKotlin {
    private val logger : Logger = LoggerFactory.getLogger(CommandKotlin::class.java)
    private val map : MutableMap<String, Command> = HashMap()

    fun registerCommand(command: Command) {
        map.put(command.name, command)
    }

    fun registerCommands(vararg commands: Command) {
        commands.forEach { registerCommand(it) }
    }

    fun executeCommand(msg: IMessage, args: Array<String>) {
        if (PermissionManager.hasPermission(msg.guild.longID, msg.author.longID, msg.author.getRolesForGuild(msg.guild).map { it.longID }, args[0])) {
            var cmd = map[args[0]]
            if (cmd != null) {
                cmd.execute(msg, args)
            } else {
                SimpleTextResponses.answer(msg, args)
            }
        }
    }
}