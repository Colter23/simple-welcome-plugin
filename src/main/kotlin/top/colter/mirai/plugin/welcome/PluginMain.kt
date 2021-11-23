package top.colter.mirai.plugin.welcome

import kotlinx.coroutines.delay
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.MemberJoinEvent
import net.mamoe.mirai.event.events.NewFriendRequestEvent
import net.mamoe.mirai.event.selectMessages
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.info
import top.colter.mirai.plugin.welcome.WelcomePluginConfig.newFriendRequest
import java.time.Instant

object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = "top.colter.mirai-example",
        name = "简单欢迎插件",
        version = "0.1.0"
    ) {
        author("Colter")
    }
) {
    override fun onEnable() {
        logger.info { "Plugin loaded" }
        val eventChannel = GlobalEventChannel.parentScope(this)

        val requestEventMap = mutableMapOf<String, NewFriendRequestEvent>()

        eventChannel.subscribeAlways<FriendMessageEvent> {
            if (message.content.startsWith("%@#=newFriendRequest=#@%")) {
                val qid = message.content.substring(24)
                val event = requestEventMap[qid]!!
                sender.sendMessage("是否同意 $qid 的好友请求\n请回复 同意 或 拒绝 或 忽略")
                selectMessages {
                    "同意" {
                        event.accept()
                        requestEventMap.remove(qid)
                        "已同意"
                    }
                    "拒绝" {
                        event.reject()
                        requestEventMap.remove(qid)
                        "已拒绝"
                    }
                    "忽略" {
                        requestEventMap.remove(qid)
                        "已忽略"
                    }
                    default { "额, 如需要继续处理请发送 /wlc req $qid" }
                    timeout(600_000) {
                        sender.sendMessage("超时, 如需要继续处理请发送 /wlc req $qid")
                    }
                }
            }
        }
        eventChannel.subscribeAlways<NewFriendRequestEvent> {

            var msg = "好友请求: $fromId $fromNick"
            if (fromGroupId != 0L) {
                msg += "\n来自: 群 $fromGroupId"
            }
            if (message.isNotEmpty()) {
                msg += "\n备注: $message"
            }
            val admin = bot.getFriend(WelcomePluginConfig.admin)

            when (newFriendRequest) {
                1 -> {
                    reject()
                    admin?.sendMessage("$msg\n处理结果: 已拒绝")
                }
                2 -> {
                    accept()
                    admin?.sendMessage("$msg\n处理结果: 已同意")
                }
                3 -> {
                    requestEventMap[fromId.toString()] = this
                    admin?.let { a ->
                        FriendMessageEvent(
                            a,
                            buildMessageChain { +PlainText("%@#=newFriendRequest=#@%$fromId") },
                            Instant.now().epochSecond.toInt()
                        ).broadcast()
                    }
                }
            }
            if (WelcomePluginConfig.friendWelcomeMessage.isNotEmpty()) {
                delay(2000)
                bot.getFriend(fromId)?.sendMessage(WelcomePluginConfig.friendWelcomeMessage)
            }
        }

        eventChannel.subscribeAlways<MemberJoinEvent> {
            if (WelcomePluginConfig.groupWelcomeMessage.isNotEmpty()) {
                group.sendMessage(At(user) + " " + WelcomePluginConfig.groupWelcomeMessage)
            }
        }
        eventChannel.subscribeAlways<BotInvitedJoinGroupRequestEvent> {
            //自动同意加群申请
//            accept()
        }
    }
}
