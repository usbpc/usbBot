package usbbot.modules

import at.mukprojects.giphy4j.Giphy
import at.mukprojects.giphy4j.exception.GiphyException
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import usbbot.commands.DiscordCommands
import usbbot.commands.core.Command
import usbbot.main.UsbBot
import org.slf4j.LoggerFactory
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.MessageHistory
import sx.blah.discord.util.RequestBuffer
import usbbot.util.MessageParsing
import usbbot.util.MessageSending
import usbbot.util.commands.AnnotationExtractor
import usbbot.util.commands.DiscordCommand
import usbbot.util.commands.DiscordSubCommand
import java.awt.Color
import java.io.ByteArrayInputStream
import java.time.LocalDateTime
import kotlin.concurrent.thread

class MiscCommands : DiscordCommands {
    val giphy = Giphy(UsbBot.getAPIKey("giphy"))
    val logger = LoggerFactory.getLogger(MiscCommands::class.java)

    override fun getDiscordCommands(): MutableCollection<Command> = AnnotationExtractor.getCommandList(this)

    @DiscordCommand("bulkdelete")
    fun bulkdelete(msg: IMessage, args: Array<String>) : Int {
        return 0
    }

    //TODO do this more better with coroutines and streams/pipes to speed things up
    private fun workingBulkdelete(history: MessageHistory) : Int {
        if (history.isEmpty()) return 0
        var numDeleted = 0
        history.spliterator()
        var deleted = history.bulkDelete()
        numDeleted += deleted.size
        history.withIndex().groupBy { Math.floor(it.index / 100.0) }
                .map { it.value }
                .forEach {
                    numDeleted += RequestBuffer.request <List<IMessage>> {
                        MessageHistory(it.map { it.value }.toList())
                                .bulkDelete()
                    }.get().size

                    logger.debug("Deleted {} of {} messages so far.", numDeleted, history.size)
                }
        return numDeleted
    }

    @DiscordSubCommand(name = "range", parent = "bulkdelete")
    fun bulkdeleteRange(msg: IMessage, args: Array<String>) {
        MessageSending.sendMessage(msg.channel, "Trying to delete messages")
        if (args.size < 4) {
            MessageSending.sendMessage(msg.channel, "Invalid Syntax.")
            return
        }
        val first = args[2].toLongOrNull()
        val second = args[3].toLongOrNull()
        if (first != null && second != null) {
            val firstMsg : IMessage? = msg.channel.getMessageByID(first)
            val secondMsg : IMessage? = msg.channel.getMessageByID(second)

            if (firstMsg == null || secondMsg == null) {
                MessageSending.sendMessage(msg.channel, "Both messages need to be in the same channel, and it has to be the channel where you execute this command!")
            } else {
                var history = if (firstMsg.timestamp.isAfter(secondMsg.timestamp)) {
                    msg.channel.getMessageHistoryIn(first, second)
                } else {
                    msg.channel.getMessageHistoryIn(second, first)
                }
                history = MessageHistory(history.filter { it.timestamp.isAfter(LocalDateTime.now().minusWeeks(2)) })
                thread (start = true, name = "Bulkdelete on guild ${msg.guild.name}") { MessageSending.sendMessage(msg.channel, "Deleted ${workingBulkdelete(history)} messages.") }
            }
        } else {
            MessageSending.sendMessage(msg.channel, "Invalid Arguments")
        }
    }

    @DiscordSubCommand(name = "last", parent = "bulkdelete")
    fun bulkdeleteLast(msg: IMessage, args: Array<String>) {
        if (args.size < 3) {
            MessageSending.sendMessage(msg.channel, "Invalid Syntax.")
        }

        val number : Int? = args[2].toIntOrNull()
        if (number != null) {
            var messageList = msg.channel.getMessageHistoryTo(LocalDateTime.now().minusWeeks(2), number).toList()
            logger.debug("Tried to get {} messages got {} messages", number, messageList.size)
            //TODO: This is a workaroung because getMessageHistoryTo is broken.
            if (messageList.size > number) {
                logger.debug("Dropping the last {} messages...", messageList.size - number)
                messageList = messageList.dropLast(messageList.size - number)
            }
            var deletedLast = RequestBuffer.request <List<IMessage>>{ MessageHistory(messageList).bulkDelete()}.get()
            var messagesDeleted = deletedLast.size
            messageList = messageList.minus(deletedLast)
            while (messageList.isNotEmpty() && deletedLast.size == 100) {
                deletedLast = RequestBuffer.request <List<IMessage>>{ MessageHistory(messageList).bulkDelete()}.get()
                messagesDeleted += deletedLast.size
                messageList = messageList.minus(deletedLast)
                logger.debug("Still have {} messages to delete", messageList.size)
            }
            if (messagesDeleted < number) {
                MessageSending.sendMessage(msg.channel, "Deleted $messagesDeleted messages. Could not delete any more, they are too old!")
            } else {
                MessageSending.sendMessage(msg.channel, "Deleted $messagesDeleted messages.")
            }
        } else {
            MessageSending.sendMessage(msg.channel, "You need to specify how many messages to delete!")
        }
    }

    @DiscordSubCommand(name = "user", parent = "bulkdelete")
    fun bulkdeleteUser(msg: IMessage, args: Array<out String>) {
        if (args.size < 4) {
            MessageSending.sendMessage(msg.channel, "Invalid Syntax.")
        } else {
            val userID = MessageParsing.getUserID(args[2])
            val count = args[3].toIntOrNull()
            if (userID != -1L) {
                if (count == null) {
                    MessageSending.sendMessage(msg.channel, "No valid message count provided.")
                    return
                }
                var messages = msg.channel.messageHistory.filter { it.author.longID == userID }.toList()

                if (messages.size - count >= 0) {
                    messages = messages.dropLast(messages.size - count)
                }

                val startingTime = LocalDateTime.now()
                val message = MessageSending.sendMessage(msg.channel, "Trying to delete ${messages.size} messages.").get()
                var history = MessageHistory(messages)

                var deleted = if (!history.isEmpty()) {
                    RequestBuffer.request <List<IMessage>> {
                        history.bulkDelete()
                    }.get()
                } else {
                    ArrayList<IMessage>()
                }

                val notDeleted = history.asArray().filter { !deleted.contains(it) }
                var deletedCount = deleted.size
                RequestBuffer.request { message.edit("Deleted $deletedCount messages. (Still running...)") }

                logger.debug("count: {}, deleteCount: {}, notDeleted: {}", count, deletedCount, notDeleted.size)

                if (count > deletedCount && notDeleted.isEmpty()) {
                    //messages = msg.channel.getMessageHistoryIn(startingTime, LocalDateTime.now().minusWeeks(2)).filter { it.author.longID == userID }.toList()
                    /*logger.debug("now, before: {} before, now: {}",
                            msg.channel.getMessageHistoryIn(startingTime, LocalDateTime.now().minusWeeks(2)).size,
                            msg.channel.getMessageHistoryIn(LocalDateTime.now().minusWeeks(2), startingTime).size)*/

                    messages = msg.channel.getMessageHistoryTo(LocalDateTime.now().minusWeeks(2)).filter { it.author.longID == userID }.filter { it.timestamp.isBefore(startingTime) }.toList()
                    if (messages.size - count >= 0) {
                        messages = messages.dropLast(messages.size - count + deletedCount)
                    }

                    while (!messages.isEmpty()) {
                        history = MessageHistory(messages)

                        if (history.size != 0) {
                            RequestBuffer.request { deleted = history.bulkDelete() }.get()
                            deletedCount += deleted.size
                        }
                        messages = messages.minus(deleted)
                    }
                } else {

                }
                if (deletedCount < count) {
                    RequestBuffer.request {
                        message.edit("Deleted $deletedCount messages. Not more messages could be deleted, because they were too old. (Done)")
                        logger.debug("A nice log message to make sure, that this is getting called {}", message.longID)
                    }.get()
                    MessageSending.sendMessage(msg.channel, "Deleted $deletedCount messages. Not more messages could be deleted, because they were too old. (Done)")
                } else {
                    RequestBuffer.request {
                        message.edit("Deleted $deletedCount messages. (Done)")
                        logger.debug("A nice log message to make sure, that this is getting called aswell {}", message.longID)
                    }.get()
                    MessageSending.sendMessage(msg.channel, "Deleted $deletedCount messages. (Done)")
                }

                MessageSending.sendMessage(msg.channel, "Done!")


            } else {
                MessageSending.sendMessage(msg.channel, "No valid user specified")
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
        if (args.size < 2) {
            MessageSending.sendMessage(msg.channel, msg.author.avatarURL)
        } else if (msg.mentions.size  == 1){
            MessageSending.sendMessage(msg.channel, msg.mentions.first().avatarURL)
        } else {
            MessageSending.sendMessage(msg.channel, "Invalid syntax")
        }
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

    @DiscordCommand("prefix")
    fun prefix(msg: IMessage, vararg args: String) {
        if (args.size < 2) MessageSending.sendMessage(msg.channel, "You need to specify a new prefix!")
        else {
            usbbot.config.setGuildCmdPrefix(msg.guild.longID, args[1])
            MessageSending.sendMessage(msg.channel, "The Command prefix is now " + args[1])
        }
    }

    @DiscordCommand("cat")
    fun cat(msg: IMessage, args: Array<String>) {
        val embed = EmbedBuilder()
                .withImage(giphy.searchRandom("cute cat").data.imageOriginalUrl)
                .withColor(Color.RED)
                //.withTitle("A cute cat:")
                //.withAuthorName(msg.client.ourUser.getDisplayName(msg.guild))
                //.withAuthorIcon(msg.client.ourUser.avatarURL)
                .withFooterText("Powered By GIPHY")
        RequestBuffer.request { msg.channel.sendMessage(embed.build()) }
    }

    @DiscordCommand("gif")
    fun giphy(msg: IMessage, args: Array<String>) {
        if (args.size < 2) return
        val builder = StringBuilder()
        args.drop(1).forEach { builder.append(it).append(" ") }
        try {
            val embed = EmbedBuilder()
                    .withImage(giphy.searchRandom(builder.toString()).data.imageOriginalUrl)
                    .withColor(Color.RED)
                    //.withTitle("A cute cat:")
                    //.withAuthorName(msg.client.ourUser.getDisplayName(msg.guild))
                    //.withAuthorIcon(msg.client.ourUser.avatarURL)
                    .withFooterText("Powered By GIPHY")
            RequestBuffer.request { msg.channel.sendMessage(embed.build()) }
        } catch (ex: GiphyException) {
           MessageSending.sendMessage(msg.channel, "Couldn't find anything for `$builder` :(")
        }
    }

    @DiscordCommand("hug")
    fun hug(msg: IMessage, args: Array<String>) {
        val delete = RequestBuffer.request { msg.delete() }
        val userID = if (args.size > 1) {
            var tmp = MessageParsing.getUserID(args[1])
            if (tmp == -1L) {
                msg.author.longID
            } else {
                tmp
            }
        } else {
            msg.author.longID
        }
        MessageSending.sendMessage(msg.channel, "*hugs <@$userID>*")
    }

    @DiscordCommand("spam")
    fun spam(msg: IMessage, args: Array<String>) {
        var msgCount = args[1].toInt()
        while (msgCount-- > 0) {
            MessageSending.sendMessage(msg.channel, msgCount.toString())
        }
    }
    @DiscordCommand("test")
    fun test(msg: IMessage, args: Array<String>) {
        val message = MessageSending.sendMessage(msg.channel, "Message to get the id.").get()
        runBlocking {
            delay(5000)
        }
        message.edit("This message is now edited!")
    }

    @DiscordCommand("massmove")
    fun massmove(msg: IMessage, args: Array<String>) {
        //TODO make this more robust
        val location = args[1].toLong()
        val goalChannel = msg.guild.getVoiceChannelByID(location)
        msg.author.getVoiceStateForGuild(msg.guild).channel.connectedUsers.forEach {
            RequestBuffer.request {
                it.moveToVoiceChannel(goalChannel)
            }
        }
    }
}