package usbbot.commands

import usbbot.commands.core.Command
import usbbot.commands.security.PermissionManager
import usbbot.config.SimpleTextCommandsSQL
import usbbot.modules.SimpleTextResponses
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.Permissions
import usbbot.util.MessageSending

class CommandHandler {

    private var cmdMap = HashMap<String, Command>()
    init {
        cmdMap.put("list", object: Command() {
            override fun execute(msg: IMessage, args: Array<String>) {
                val iterator = cmdMap.keys.iterator()
                val builder = StringBuilder()
                while (iterator.hasNext()) {
                    builder.append('`').append(iterator.next()).append("`").append(", ")
                }
                val iterator2 = SimpleTextCommandsSQL.getAllCommandsForServer(msg.guild.longID).iterator()
                while (iterator2.hasNext()) {
                    builder.append('`').append(iterator2.next().key).append("`")
                    if (iterator2.hasNext()) {
                        builder.append(", ")
                    }
                }
                MessageSending.sendMessage(msg.channel, "Commands are: " + builder.toString())
            }
        })
    }

    fun registerCommand(cmd: Command) = cmdMap.put(cmd.name, cmd)
    fun registerCommands(cmds: DiscordCommands) = cmds.discordCommands.forEach({registerCommand(it)})
    fun discordCommandExists(name: String, guildID: Long) : Boolean {
        if (cmdMap.containsKey(name)) return true
        //FIXME: This only exists so that the SimpleTextResponses Module knows that a command with that name is already registered, so remove this
        if (SimpleTextCommandsSQL.getAllCommandsForServer(guildID).containsKey(name)) return true
        return false
    }
    fun getArguments(input: String, prefix: String): Array<String> {
        return input.substring(input.indexOf(prefix) + prefix.length).split(" +".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
    }
    fun onMessageRecivedEvent(event: MessageReceivedEvent) {
        //Check that the message was not send in a private channel, if it was just ignore it.
        if (!event.channel.isPrivate) {
            //TODO: Check if the message is on the word/regex blacklist, remove it if it is (blacklist may not apply to all users)
            //Check if the message starts with the server command prefix, if not ignore it
            val prefix = usbbot.config.getGuildCmdPrefix(event.guild.longID)
            if (event.message.content.startsWith(prefix)) {
                val args = getArguments(event.message.content, prefix)
                //check if the message contains a valid command for that guild and check permissions
                //I need to check if the command exists before testing for permissions, otherwise a permission entry will be created
                if(cmdMap.containsKey(args[0]) || SimpleTextCommandsSQL.getAllCommandsForServer(event.guild.longID).containsKey(args[0])) {
                    val isAdministrator = event.author.getPermissionsForGuild(event.guild).contains(Permissions.ADMINISTRATOR)
                    val hasPermission = PermissionManager.hasPermission(
                            event.guild.longID,
                            event.author.longID,
                            event.message.guild.getRolesForUser(event.author).map { it.longID },
                            args[0])
                    if(isAdministrator || hasPermission) {
                        if (cmdMap.containsKey(args[0])) {
                            cmdMap.get(args[0])?.execute(event.message, args)
                        } else {
                            SimpleTextResponses.answer(event.message, args)
                        }
                    } else {
                        MessageSending.sendMessage(event.channel, "You don't have permissions required to use this command!")
                    }
                }


            }
        }
    }
}
