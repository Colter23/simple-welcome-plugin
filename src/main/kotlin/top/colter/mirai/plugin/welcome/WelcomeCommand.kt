package top.colter.mirai.plugin.welcome

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CommandSenderOnMessage
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.descriptor.CommandArgumentParserException
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.buildMessageChain
import java.time.Instant

class WelcomeCommand : CompositeCommand(
    owner = PluginMain,
    "wlc"
) {

    @SubCommand("req", "请求")
    suspend fun CommandSenderOnMessage<FriendMessageEvent>.set(qid: Long) {
        FriendMessageEvent(
            fromEvent.sender,
            buildMessageChain { +PlainText("%@#=newFriendRequest=#@%$qid") },
            fromEvent.time
        ).broadcast()
    }

}

