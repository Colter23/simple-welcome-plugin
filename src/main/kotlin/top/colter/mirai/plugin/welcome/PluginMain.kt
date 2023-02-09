package top.colter.mirai.plugin.welcome

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.permission.PermissionId
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.permission.PermissionService.Companion.getPermittedPermissions
import net.mamoe.mirai.console.permission.PermitteeId.Companion.permitteeId
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.event.*
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.info
import top.colter.mirai.plugin.welcome.WelcomePluginConfig.botInvitedJoinGroupRequest
import top.colter.mirai.plugin.welcome.WelcomePluginConfig.friendWelcomeMessage
import top.colter.mirai.plugin.welcome.WelcomePluginConfig.groupWelcomeMessage
import top.colter.mirai.plugin.welcome.WelcomePluginConfig.newFriendRequest
import java.io.InputStream
import java.net.URL

object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = "top.colter.simple-welcome",
        name = "SimpleWelcome",
        version = "1.1.0"
    ) {
        author("Colter")
    }
) {

    var admin: Friend? = null

    @OptIn(MiraiExperimentalApi::class)
    override fun onEnable() {
        logger.info { "Plugin loaded" }

        WelcomeCommand.register()
        GroupCommand.register()

        WelcomePluginConfig.reload()
        WelcomePluginData.reload()

        val gwp = PermissionId("group","welcome.message")
        PermissionService.INSTANCE.register(gwp,"群欢迎语")

        val eventChannel = GlobalEventChannel.parentScope(this)
        val requestMap = mutableMapOf<Long, BotEvent>()

        eventChannel.subscribeAlways<GroupMessageEvent> {
            val hasPerm = group.permitteeId.getPermittedPermissions().any { it.id == gwp }
            if (hasPerm && message.content == "#r"){
                group.sendMessage(QuoteReply(source) + Dice((1..6).random()))
            }
        }

        eventChannel.subscribeAlways<MemberJoinEvent> {

            val groupName = it.group.name
            val userName = it.user.nick
            val userAvatarUrl = it.user.avatarUrl//获取基础信息，如群名，用户名，用户头像url

            var message = groupWelcomeMessage
            message = message.replace("${'$'}{群名}",groupName)
                .replace("${'$'}{用户名}",userName)//替换非图像类的用户信息

            val userAvatarUrlInputStream : InputStream =
                withContext(Dispatchers.IO) {
                    URL(userAvatarUrl).openStream()
                }
            logger.info { "userAvatarUrl:${userAvatarUrl}" }
            val userAvatarImage :Image
            userAvatarUrlInputStream.use {
                userAvatarImage = userAvatarUrlInputStream.uploadAsImage(group)
            }//获取用户头像，生成Image对象

            val hasPerm = group.permitteeId.getPermittedPermissions().any { it.id == gwp }
            if (hasPerm && message.isNotEmpty()) {
                val messageSendListData =  message.split("${'$'}{头像}")
                var messageSend : Message = At(user)

                if (messageSendListData.size == 1){//判断是否需要替换头像，如list长度为一，则不需要替换头像
                    messageSend += messageSendListData[0]

                }else if (messageSendListData.size >= 2){
                    val iterator = messageSendListData.iterator()
                    messageSend += iterator.next()
                    while (iterator.hasNext()){
                        messageSend += userAvatarImage
                        messageSend += iterator.next()
                    }
                }
                group.sendMessage(messageSend)
            }
        }

        eventChannel.subscribeAlways<NewFriendRequestEvent> {
            val msg = buildString {
                append("Bot好友请求: $fromId@$fromNick")
                if (fromGroupId != 0L) {
                    append("\n来自群: ${fromGroup?.name}")
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
                    requestMap[fromId] = this
                    admin?.sendMessage("$msg\n请回复 同意 或 拒绝 或 忽略")
                }
            }
            if (friendWelcomeMessage.isNotEmpty()) {
                delay(2000)
                bot.getFriend(fromId)?.sendMessage(friendWelcomeMessage)
            }
        }

        eventChannel.subscribeAlways<BotInvitedJoinGroupRequestEvent> {
            val msg = "Bot群邀请请求: $groupId@$groupName\n邀请人: $invitorId@$invitorNick"
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
                    requestMap[groupId * -1] = this
                    admin?.sendMessage("$msg\n请回复 同意 或 拒绝 或 忽略")
                }
            }
        }

        eventChannel.subscribeAlways<BotJoinGroupEvent> {
            val admin = bot.getFriend(WelcomePluginConfig.admin)
            admin?.sendMessage("Bot加入群: ${group.name}@${groupId}")
        }

        eventChannel.subscribeAlways<FriendMessageEvent>{
            if (sender.id == WelcomePluginConfig.admin){
                if (listOf("同意","拒绝","忽略","全部同意","全部拒绝","全部忽略","逐个处理").contains(message.content)){
                    if (requestMap.isNotEmpty()){
                        if (requestMap.size == 1 || listOf("全部同意","全部拒绝","全部忽略").contains(message.content)){
                            when(message.content){
                                "同意","全部同意" -> {
                                    requestMap.forEach { (k, v) ->
                                        if (k < 0) (v as BotInvitedJoinGroupRequestEvent).accept() else (v as NewFriendRequestEvent).accept()
                                    }
                                    requestMap.clear()
                                }
                                "拒绝","全部拒绝" -> {
                                    requestMap.forEach { (k, v) ->
                                        if (k < 0) (v as BotInvitedJoinGroupRequestEvent).ignore() else (v as NewFriendRequestEvent).reject()
                                    }
                                    requestMap.clear()
                                }
                                "忽略","全部忽略" -> {
                                    requestMap.clear()
                                }
                            }
                            sender.sendMessage("处理完成")
                        }else if (message.content == "逐个处理"){
                            sender.sendMessage("请回复 同意 or 拒绝 or 忽略")
                            val reqMap = requestMap.toMap()
                            reqMap.forEach { (k, v) ->
                                if (k<0){
                                    val g = v as BotInvitedJoinGroupRequestEvent
                                    sender.sendMessage("群请求: ${g.groupId}@${g.groupName}")
                                }else{
                                    val f = v as NewFriendRequestEvent
                                    sender.sendMessage("好友请求: ${f.fromId}@${f.fromNick}")
                                }
                                selectMessagesUnit {
                                    "同意" {
                                        if (k < 0) (v as BotInvitedJoinGroupRequestEvent).accept() else (v as NewFriendRequestEvent).accept()
                                        requestMap.remove(k)
                                    }
                                    "拒绝" {
                                        if (k < 0) (v as BotInvitedJoinGroupRequestEvent).ignore() else (v as NewFriendRequestEvent).reject()
                                        requestMap.remove(k)
                                    }
                                    "忽略" {
                                        requestMap.remove(k)
                                    }
                                    default { sender.sendMessage("匹配失败") }
                                    timeout(600_000) {
                                        sender.sendMessage("超时")
                                        return@timeout
                                    }
                                }
                            }
                            sender.sendMessage("处理完成")
                        } else{
                            sender.sendMessage(buildString {
                                appendLine("多个请求待处理")
                                val friendReq = requestMap.filter { it.key > 0 }.mapValues { it.value as NewFriendRequestEvent }
                                val groupReq = requestMap.filter { it.key < 0 }.mapValues { it.value as BotInvitedJoinGroupRequestEvent }
                                if (friendReq.isNotEmpty()) appendLine("好友请求:")
                                friendReq.forEach { (_, f) -> appendLine("${f.fromId}@${f.fromNick}") }
                                if (groupReq.isNotEmpty()) appendLine("群请求:")
                                groupReq.forEach { (_, f) -> appendLine("${f.groupId}@${f.groupName}") }
                                appendLine("共 ${requestMap.size} 个请求")
                                appendLine("请再次发送 全部同意 or 全部拒绝 or 全部忽略 or 逐个处理")
                            })
                        }
                    }else{
                        sender.sendMessage("最近没有请求哦")
                    }
                }
            }
        }
    }

    override fun onDisable() {
        WelcomeCommand.unregister()
        GroupCommand.unregister()
    }
}
