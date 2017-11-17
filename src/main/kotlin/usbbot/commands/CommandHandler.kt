package usbbot.commands

import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newFixedThreadPoolContext
import kotlinx.coroutines.experimental.newSingleThreadContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import usbbot.commands.core.Command
import usbbot.commands.security.PermissionManager
import usbbot.modules.SimpleTextResponses
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.Permissions
import usbbot.config.*
import usbbot.util.MessageSending
import kotlin.system.measureTimeMillis

class CommandHandler {
    companion object {
        val logger : Logger = LoggerFactory.getLogger(CommandHandler::class.java)
        val stc = newFixedThreadPoolContext(4,"commandExecutor")
    }
    private var cmdMap = HashMap<String, Command>()
    init {
        cmdMap.put("list", object: Command() {
            override fun execute(msg: IMessage, args: Array<String>) {
                val iterator = cmdMap.keys.iterator()
                val builder = StringBuilder()
                while (iterator.hasNext()) {
                    builder.append('`').append(iterator.next()).append("`").append(", ")
                }

                var iterator2 : Iterator<DBTextCommand>? = null
                val time = measureTimeMillis {
                    iterator2 = getDBTextCommandsForGuild(msg.guild.longID).iterator()
                }
                logger.trace("It took me {}ms to look up how many text commands there are.", time)

                while (iterator2!!.hasNext()) {
                    builder.append('`').append(iterator2!!.next().name).append("`")
                    if (iterator2!!.hasNext()) {
                        builder.append(", ")
                    }
                }
                MessageSending.sendMessage(msg.channel, "Commands are: " + builder.toString())
            }
        })
    }
    fun createMissingPermissions(guildID: Long) {
        var guildCommands = getCommandsForGuild(guildID)
        cmdMap.keys.filter { cmdName -> guildCommands.find { it.name == cmdName} == null }.forEach {
            createDBCommand(guildID, it, "whitelist", "whitelist")
        }
    }
    fun registerCommand(cmd: Command) = cmdMap.put(cmd.name, cmd)
    fun registerCommands(cmds: DiscordCommands) = cmds.discordCommands.forEach({registerCommand(it)})
    fun discordCommandExists(name: String, guildID: Long) : Boolean {
        if (cmdMap.containsKey(name)) return true
        //FIXME: This only exists so that the SimpleTextResponses Module knows that a command with that name is already registered, so remove this
        if (getCommandForGuild(guildID, name) != null) return true
        return false
    }
    fun getArguments(input: String, prefix: String): Array<String> {
        return input.substring(input.indexOf(prefix) + prefix.length).split(" +".toRegex()).dropWhile({ it.isEmpty() }).toTypedArray()
    }
    fun onMessageRecivedEvent(event: MessageReceivedEvent) {
        //Check that the message was not send in a private channel, if it was just ignore it.
        //repeat(10) {
            val timeForCoroutineStart = measureTimeMillis {
                launch(stc) {
                    val timeCoroutineStart = System.currentTimeMillis()
                        if (!(event.author.isBot || event.channel.isPrivate)) {
                            //TODO: Check if the message is on the word/regex blacklist, remove it if it is (blacklist may not apply to all users)
                            //Check if the message starts with the server command prefix, if not ignore it
                            val guild = getGuildById(event.guild.longID) ?:
                                    throw IllegalStateException("A Command was tried to be executed on a Guild that has no DB entry")

                            if (event.message.content.startsWith(guild.prefix)) {
                                val args = getArguments(event.message.content, guild.prefix)
                                //check if the message contains a valid command for that guild and check permissions
                                //I need to check if the command exists before testing for permissions, otherwise a permission entry will be created
                                logger.trace("IT took me {}ms to get before StupidWrapper", System.currentTimeMillis() - timeCoroutineStart)
                                val stupidWrapper = StupidWrapper()

                                logger.trace("IT took me {}ms to get after StupidWrapper", System.currentTimeMillis() - timeCoroutineStart)
                                if(stupidWrapper.isCommand(cmdMap, event.guild.longID, args[0])) {

                                    logger.trace("IT took me {}ms to check if it is a command", System.currentTimeMillis() - timeCoroutineStart)
                                    val isAdministrator = event.author.getPermissionsForGuild(event.guild).contains(Permissions.ADMINISTRATOR)

                                    if(isAdministrator || PermissionManager.hasPermission(
                                            event.guild.longID,
                                            event.author.longID,
                                            event.message.guild.getRolesForUser(event.author).map { it.longID },
                                            args[0])) {

                                        logger.trace("IT took me {}ms to check permissions (successful)", System.currentTimeMillis() - timeCoroutineStart)
                                        if (stupidWrapper.stc == null) {
                                            cmdMap[args[0]]?.execute(event.message, args)
                                        } else {
                                            SimpleTextResponses.answer(event.message, stupidWrapper.stc)
                                        }
                                    } else {
                                        logger.trace("IT took me {}ms to check permissions (unsuccessful)", System.currentTimeMillis() - timeCoroutineStart)
                                        MessageSending.sendMessage(event.channel, "You don't have permissions required to use this command!")
                                    }
                                    logger.trace("IT took me {}ms to run the command", System.currentTimeMillis() - timeCoroutineStart)
                                }
                            }
                        }
                    logger.trace("It took me {} ms to execute the Coroutine for message {}", System.currentTimeMillis() - timeCoroutineStart, event.message.content)
                }
            }
            logger.trace("It took me {} ms to start the Coroutine for message {}", timeForCoroutineStart, event.message.content)
    //    }


    }

}

private class StupidWrapper {
    var stc : DBTextCommand? = null
    fun isCommand(cmdMap: HashMap<String, Command>, guildID: Long, name: String) : Boolean {
        if (cmdMap.containsKey(name)) return true
        stc = getDBTextCommand(guildID, name)
        if (stc != null) return true
        return false
    }
}
