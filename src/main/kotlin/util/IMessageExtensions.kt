package util

import kotlinx.coroutines.experimental.newFixedThreadPoolContext
import kotlinx.coroutines.experimental.run
import sx.blah.discord.api.internal.json.objects.EmbedObject
import sx.blah.discord.handle.obj.*
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.PermissionUtils
import sx.blah.discord.util.RequestBuffer
import usbbot.util.MessageSending
import java.awt.Color
import java.util.concurrent.Future

//private val futureWaiterPool = newFixedThreadPoolContext(5, "Future Waiter Pool")

fun getDefaultEmbed(color: Color, message: String) : EmbedObject =
        getDefaultEmbedBuilder(color).withDescription(message).build()

fun getDefaultEmbedBuilder(color: Color) : EmbedBuilder =
        EmbedBuilder().withColor(color)

fun IChannel.checkPermissions(user: IUser, vararg permissions: Permissions) : Boolean {
    return if (PermissionUtils.hasPermissions(this, user, *permissions)) {
        true
    } else {
        //MessageSending.sendMessage(this, "Nope, don't have permissions!")
        false
    }
}

fun IGuild.checkPermissions(user: IUser, vararg permissions: Permissions) : Boolean =
        PermissionUtils.hasPermissions(this, user, *permissions)

fun IGuild.checkOurPermissions(vararg permissions: Permissions) : Boolean =
        this.checkPermissions(this.client.ourUser, *permissions)

fun IChannel.checkOurPermissions(vararg permissions: Permissions) : Boolean =
        this.checkPermissions(this.client.ourUser, *permissions)

fun Future<IMessage>.updateSuccess(message: String) : Future<IMessage> =
        this.get().bufferedEdit(getDefaultEmbed(Color.GREEN, message))

fun Future<IMessage>.updateError(message: String) : Future<IMessage> =
        this.get().bufferedEdit(getDefaultEmbed(Color.RED, message))

fun Future<IMessage>.uptadeProcessing(message: String) : Future<IMessage> =
        this.get().bufferedEdit(getDefaultEmbed(Color.YELLOW, message))

fun IChannel.sendSuccess(message: String) : Future<IMessage> =
        this.bufferedSend(getDefaultEmbed(Color.GREEN, message))

fun IChannel.sendProcessing(message: String) =
        this.bufferedSend(getDefaultEmbed(Color.YELLOW, message))

fun IChannel.sendError(message: String) : Future<IMessage> =
        this.bufferedSend(getDefaultEmbed(Color.RED, message))

fun IMessage.bufferedEdit(embed: EmbedObject) : Future<IMessage> =
        RequestBuffer.request <IMessage> { this.edit(embed) }

fun IChannel.bufferedSend(embed: EmbedObject) : Future<IMessage> =
        RequestBuffer.request <IMessage> { this.sendMessage(embed) }