package top.colter.mirai.plugin.welcome

import kotlinx.coroutines.delay
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.permission.PermissionId
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.permission.PermissionService.Companion.getPermittedPermissions
import net.mamoe.mirai.console.permission.PermitteeId.Companion.permitteeId
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.event.selectMessages
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.info
import top.colter.mirai.plugin.welcome.WelcomePluginConfig.botInvitedJoinGroupRequest
import top.colter.mirai.plugin.welcome.WelcomePluginConfig.friendWelcomeMessage
import top.colter.mirai.plugin.welcome.WelcomePluginConfig.groupWelcomeMessage
import top.colter.mirai.plugin.welcome.WelcomePluginConfig.newFriendRequest
import java.time.Instant

object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = "top.colter.simple-welcome",
        name = "简单欢迎插件",
        version = "0.1.0"
    ) {
        author("Colter")
    }
) {
    override fun onEnable() {
        logger.info { "Plugin loaded" }

        WelcomeCommand.register()
        GroupCommand.register()

        val gwp = PermissionId("group","welcome.message")
        PermissionService.INSTANCE.register(gwp,"群欢迎语")

        val eventChannel = GlobalEventChannel.parentScope(this)
        val friendRequestMap = mutableMapOf<String, NewFriendRequestEvent>()
        val botInvitedJoinGroupMap = mutableMapOf<String, BotInvitedJoinGroupRequestEvent>()

        eventChannel.subscribeAlways<GroupMessageEvent> {
            val hasPerm = group.permitteeId.getPermittedPermissions().any { it.id == gwp }
            if (hasPerm && message.content == "#r"){
                group.sendMessage(QuoteReply(source) + Dice((1..6).random()))
            }
        }
        eventChannel.subscribeAlways<FriendMessageEvent> {
            if (message.content.startsWith("%@#=NewFriendRequest=#@%")) {
                val qid = message.content.substring(24)
                val event = friendRequestMap[qid]!!
                sender.sendMessage("是否同意 $qid 的好友请求\n请回复 同意 或 拒绝 或 忽略")
                selectMessages {
                    "同意" {
                        event.accept()
                        friendRequestMap.remove(qid)
                        "已同意"
                    }
                    "拒绝" {
                        event.reject()
                        friendRequestMap.remove(qid)
                        "已拒绝"
                    }
                    "忽略" {
                        friendRequestMap.remove(qid)
                        "已忽略"
                    }
                    default { "额, 如需要继续处理请发送 /wlc freq $qid" }
                    timeout(600_000) {
                        sender.sendMessage("超时, 如需要继续处理请发送 /wlc freq $qid")
                    }
                }
            }else if(message.content.startsWith("%@#=BotInvitedJoinGroupRequest=#@%")){
                val gid = message.content.substring(34)
                val event = botInvitedJoinGroupMap[gid]!!
                sender.sendMessage("是否同意bot加入群 $gid\n请回复 同意 或 拒绝 或 忽略")
                selectMessages {
                    "同意" {
                        event.accept()
                        friendRequestMap.remove(gid)
                        "已同意"
                    }
                    "拒绝" {
                        event.ignore()
                        friendRequestMap.remove(gid)
                        "已拒绝"
                    }
                    "忽略" {
                        friendRequestMap.remove(gid)
                        "已忽略"
                    }
                    default { "额, 如需要继续处理请发送 /wlc greq $gid" }
                    timeout(600_000) {
                        sender.sendMessage("超时, 如需要继续处理请发送 /wlc req $gid")
                    }
                }
            }
        }
        eventChannel.subscribeAlways<NewFriendRequestEvent> {
            val msg = buildString {
                append("好友请求: $fromId $fromNick")
                if (fromGroupId != 0L) {
                    append("\n来自: 群 ${fromGroup?.name}")
                }
                if (message.isNotEmpty()) {
                    append("\n备注: $message")
                }
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
                    friendRequestMap[fromId.toString()] = this
                    admin?.let { a ->
                        FriendMessageEvent(
                            a,
                            buildMessageChain { +PlainText("%@#=NewFriendRequest=#@%$fromId") },
                            Instant.now().epochSecond.toInt()
                        ).broadcast()
                    }
                }
            }
            if (friendWelcomeMessage.isNotEmpty()) {
                delay(2000)
                bot.getFriend(fromId)?.sendMessage(friendWelcomeMessage)
            }
        }

        eventChannel.subscribeAlways<BotInvitedJoinGroupRequestEvent> {
            val msg = "群邀请请求: $groupId $groupName\n邀请人: $invitorId $invitorNick"
            val admin = bot.getFriend(WelcomePluginConfig.admin)
            when (botInvitedJoinGroupRequest) {
                1 -> {
                    ignore()
                    admin?.sendMessage("$msg\n处理结果: 已忽略")
                }
                2 -> {
                    accept()
                    admin?.sendMessage("$msg\n处理结果: 已同意")
                }
                3 -> {
                    botInvitedJoinGroupMap[groupId.toString()] = this
                    admin?.let { a ->
                        FriendMessageEvent(
                            a,
                            buildMessageChain { +PlainText("%@#=BotInvitedJoinGroupRequest=#@%$groupId") },
                            Instant.now().epochSecond.toInt()
                        ).broadcast()
                    }
                }
            }
        }

        eventChannel.subscribeAlways<MemberJoinEvent> {
            val hasPerm = group.permitteeId.getPermittedPermissions().any { it.id == gwp }
            if (hasPerm && groupWelcomeMessage.isNotEmpty()) {
                group.sendMessage(At(user) + " " + groupWelcomeMessage)
            }
        }
    }
}
