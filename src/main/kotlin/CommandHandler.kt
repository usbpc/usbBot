import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

fun onMessageRecivedEvent(event: MessageReceivedEvent) {
    //Check that the message was not send in a private channel, if it was just ignore it.
    if (!event.channel.isPrivate) {
        //TODO:Check if the message is on the word/regex blacklist, remove it if it is (blacklist may not apply to all users)
        //Check if the message starts with the server command prefix, if not ignore it
        if (event.message.content.startsWith("!")) {

        }
        //check if the message contains a valid command for that server and check permissions
    }
}