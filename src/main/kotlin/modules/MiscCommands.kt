package modules

import at.mukprojects.giphy4j.Giphy
import commands.DiscordCommands
import commands.core.Command
import main.UsbBot
import org.slf4j.LoggerFactory
import sx.blah.discord.handle.impl.obj.Message
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.MessageHistory
import sx.blah.discord.util.RequestBuffer
import util.MessageParsing
import util.MessageSending
import util.commands.AnnotationExtractor
import util.commands.DiscordCommand
import util.commands.DiscordSubCommand
import java.awt.Color
import java.io.ByteArrayInputStream
import java.time.LocalDateTime

class MiscCommands : DiscordCommands {
    val giphy = Giphy(UsbBot.getAPIKey("giphy"))
    val logger = LoggerFactory.getLogger(MiscCommands::class.java)

    override fun getDiscordCommands(): MutableCollection<Command> = AnnotationExtractor.getCommandList(this)

    @DiscordCommand("bulkdelete")
    fun bulkdelete(msg: IMessage, args: Array<String>) : Int {
        return 0
    }
    //TODO: Deal if more messages than 100 are provided
    //TODO: Deal if messages older than 2 week are provided
    @DiscordSubCommand(name = "range", parent = "bulkdelete")
    fun bulkdeleteRange(msg: IMessage, args: Array<String>) {
        if (args.size < 4) {
            MessageSending.sendMessage(msg.channel, "Invalid Syntax.")
            return
        }
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
                MessageSending.sendMessage(msg.channel, "Deleted " + history.size + " messages.")
                history.bulkDelete()
            }
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
                val message = MessageSending.sendMessage(msg.channel, "Trying to delete ${messages.size} messages.")
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
            config.setGuildCmdPrefix(msg.guild.longID, args[1])
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
        msg.channel.sendMessage(embed.build())
    }

    @DiscordCommand("giphy")
    fun giphy(msg: IMessage, args: Array<String>) {
        if (args.size < 2) return
        val builder = StringBuilder()
        args.drop(1).forEach { builder.append(it).append(" ") }
        val embed = EmbedBuilder()
                .withImage(giphy.searchRandom(builder.toString()).data.imageOriginalUrl)
                .withColor(Color.RED)
                //.withTitle("A cute cat:")
                //.withAuthorName(msg.client.ourUser.getDisplayName(msg.guild))
                //.withAuthorIcon(msg.client.ourUser.avatarURL)
                .withFooterText("Powered By GIPHY")
        msg.channel.sendMessage(embed.build())
    }
}