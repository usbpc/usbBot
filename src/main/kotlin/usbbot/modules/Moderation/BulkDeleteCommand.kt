package usbbot.modules.Moderation

import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ClosedSendChannelException
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newSingleThreadContext
import org.slf4j.LoggerFactory
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.util.MessageHistory
import usbbot.commands.DiscordCommands
import usbbot.commands.core.Command
import usbbot.util.MessageParsing
import usbbot.util.commands.AnnotationExtractor
import usbbot.util.commands.DiscordCommand
import usbbot.util.commands.DiscordSubCommand
import util.*
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.Future

class BulkDeleteCommand : DiscordCommands {
    private val logger = LoggerFactory.getLogger(BulkDeleteCommand::class.java)
    override fun getDiscordCommands(): MutableCollection<Command> = AnnotationExtractor.getCommandList(this)

    private val bulkDeleteCoroutineContext = newSingleThreadContext("Bulk delete")

    @DiscordCommand("bulkdelete")
    fun bulkdelete(msg: IMessage, args: Array<String>) : Int {
        return 0
    }

    @DiscordSubCommand(name = "range", parent = "bulkdelete")
    fun bulkdeleteRange(msg: IMessage, args: Array<String>) {
        launch(bulkDeleteCoroutineContext) {
            if (args.size < 4) {
                msg.channel.sendError("Invalid Syntax.")
                return@launch
            }
            val first = args[2].toLongOrNull()
            val second = args[3].toLongOrNull()
            if (first == null || second == null) {
                msg.channel.sendError("Please specify two valid message ids!")
                return@launch
            }
            val firstMsg : IMessage? = msg.channel.getMessageByID(first)
            val secondMsg : IMessage? = msg.channel.getMessageByID(second)
            if (firstMsg == null || secondMsg == null) {
                msg.channel.sendError("Both messages need to be in the same channel, and it has to be the channel where you execute this command!")
                return@launch
            }
            val latestMessageId : Long
            val oldestMessageId : Long
            if (firstMsg.timestamp.isAfter(secondMsg.timestamp)) {
                latestMessageId = firstMsg.longID
                oldestMessageId = secondMsg.longID
            } else {
                latestMessageId = secondMsg.longID
                oldestMessageId = firstMsg.longID
            }

            bulkDeleteHelper(msg.channel.sendProcessing("Starting to delete messages..."), msg.channel, latestMessageId) { inputChannel, collectedMessages ->
                var msgList = mutableListOf<IMessage>()
                var message = inputChannel.receive()
                while (message.longID != oldestMessageId) {
                    msgList = msgList.addAndSend(message, collectedMessages)
                    message = inputChannel.receive()
                }
                msgList = msgList.addAndSend(message, collectedMessages)
                //Send the last messages of to be deleted if the last List isn't empty!
                if (msgList.size > 0) {
                    collectedMessages.send(msgList)
                }

                //Close channels!
                logger.trace("Closing channels...")
                collectedMessages.close()
                inputChannel.closeFromWrongSide()
            }
        }
    }

    @DiscordSubCommand(name = "last", parent = "bulkdelete")
    fun bulkdeleteLast(msg: IMessage, args: Array<String>) {
        launch(bulkDeleteCoroutineContext) {
            if (args.size < 3) {
                msg.channel.sendError("Invalid Syntax.")
                return@launch
            }
            val number : Int? = args[2].toIntOrNull()
            if (number == null) {
                msg.channel.sendError("You need to specify how many messages to delete!")
                return@launch
            }

            bulkDeleteHelper(msg.channel.sendProcessing("Starting to delete messages..."), msg.channel, msg.longID) { inputChannel, collectedMessages ->
                var deleted = 0
                var msgList = mutableListOf<IMessage>()
                while (deleted < number) {
                    inputChannel.receive().let {
                        //We've got a message that shall be deleted, so increment the deleted counter!
                        deleted++
                        //Now add that message to the list of messages to delete
                        msgList = msgList.addAndSend(it, collectedMessages)
                    }
                }
                //Send the last messages of to be deleted if the last List isn't empty!
                if (msgList.size > 0) {
                    collectedMessages.send(msgList)
                }

                //Close channels!
                logger.trace("Closing channels...")
                collectedMessages.close()
                inputChannel.closeFromWrongSide()
            }
        }
    }

    @DiscordSubCommand(name = "user", parent = "bulkdelete")
    fun bulkdeleteUser(msg: IMessage, args: Array<out String>) {
        launch(bulkDeleteCoroutineContext) {
            if (args.size < 4) {
                msg.channel.sendError("Invalid Syntax.")
                return@launch
            }
            val userID = MessageParsing.getUserID(args[2])
            val number = args[3].toIntOrNull()
            if (userID == -1L) {
                msg.channel.sendError("Specify a valid user!")
                return@launch
            }
            if (number == null) {
                msg.channel.sendError("No valid message count provided.")
                return@launch
            }

            bulkDeleteHelper(msg.channel.sendProcessing("Starting to delete messages..."), msg.channel, msg.longID) { inputChannel, collectedMessages ->
                var deleted = 0
                var msgList = mutableListOf<IMessage>()
                while (deleted < number) {
                    inputChannel.receive().let {
                        if (it.author.longID == userID) {
                            //We've got a message that shall be deleted, so increment the deleted counter!
                            deleted++
                            //Now add that message to the list of messages to delete
                            msgList = msgList.addAndSend(it, collectedMessages)
                        }
                    }
                }
                //Send the last messages of to be deleted if the last List isn't empty!
                if (msgList.size > 0) {
                    collectedMessages.send(msgList)
                }

                //Close channels!
                logger.trace("Closing channels...")
                collectedMessages.close()
                inputChannel.closeFromWrongSide()

            }

        }
    }

    fun bulkDeleteMessageGetter(discordChannel: IChannel, startId: Long, sendChannel: Channel<IMessage>) = launch(bulkDeleteCoroutineContext) {
        var startId = startId
        while (!sendChannel.isClosedForSend) {
            val msgHistory = discordChannel.getMessageHistoryFrom(startId, 100)
            startId = msgHistory.earliestMessage.longID

            for (message in msgHistory) {
                if (message.longID == startId) continue
                var endCondition = false
                try {
                    sendChannel.send(message)
                } catch (ex: ClosedSendChannelException) {
                    logger.debug("Input Channel was closed!")
                    endCondition = true
                }
                if (endCondition) break
            }
        }
    }

    suspend fun <T> Channel<T>.closeFromWrongSide() {
        this.close()
        this.consumeEach {}
    }

    suspend fun bulkDeleteHelper(progressMsg: Future<IMessage>, channel: IChannel, startId: Long, block: suspend (Channel<IMessage>, Channel<Collection<IMessage>>) -> Unit) {
        val inputChannel = Channel<IMessage>()
        val outputChannel = Channel<Collection<IMessage>>()
        bulkDeleteMessageGetter(channel, startId, inputChannel)

        launch(bulkDeleteCoroutineContext) {
            block(inputChannel, outputChannel)
        }

        var progressMsg = progressMsg
        var counter = 0
        outputChannel.consumeEach {
            val msgHistory = MessageHistory(it)
            //Making sure that I don't try to delete messages older than 2 weeks... because discord and stuff!
            if (msgHistory.latestMessage.creationDate.atZone(ZoneId.of("UTC")).isAfter(LocalDateTime.now().atZone(ZoneId.of("UTC")).minusWeeks(2))) {
                counter += msgHistory.bufferedBulkDelete().get()?.size ?: 0
                logger.trace("Before sending the message update!")
                progressMsg = progressMsg.uptadeProcessing("Deleted $counter messages so far...")
                logger.trace("After sending the message update!")
            }
        }
        logger.trace("Before sending the end update")
        progressMsg.updateSuccess("Deleted $counter messages!")
        logger.trace("After sending the end update")
    }

    suspend fun MutableList<IMessage>.addAndSend(msg: IMessage, channel: Channel<Collection<IMessage>>) : MutableList<IMessage> {
        this.add(msg)
        return if (this.size == 100) {
            channel.send(this)
            mutableListOf()
        } else {
            this
        }
    }
}