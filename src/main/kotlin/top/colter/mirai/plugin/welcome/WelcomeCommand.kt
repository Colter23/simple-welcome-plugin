package top.colter.mirai.plugin.welcome

import net.mamoe.mirai.console.command.CommandSenderOnMessage
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.permission.AbstractPermitteeId
import net.mamoe.mirai.console.permission.PermissionId
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.permission.PermissionService.Companion.permit
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.buildMessageChain

object WelcomeCommand : CompositeCommand(
    owner = PluginMain,
    "wlc"
) {

    @SubCommand("freq", "好友请求")
    suspend fun CommandSenderOnMessage<FriendMessageEvent>.freq(qid: Long) {
        if (this.fromEvent.sender.id == WelcomePluginConfig.admin){
            FriendMessageEvent(
                fromEvent.sender,
                buildMessageChain { +PlainText("%@#=NewFriendRequest=#@%$qid") },
                fromEvent.time
            ).broadcast()
        }
    }

    @SubCommand("greq", "群请求")
    suspend fun CommandSenderOnMessage<FriendMessageEvent>.greq(gid: Long) {
        if (this.fromEvent.sender.id == WelcomePluginConfig.admin){
            FriendMessageEvent(
                fromEvent.sender,
                buildMessageChain { +PlainText("%@#=BotInvitedJoinGroupRequest=#@%$gid") },
                fromEvent.time
            ).broadcast()
        }
    }

    @SubCommand("group", "g","群")
    suspend fun CommandSenderOnMessage<FriendMessageEvent>.group(page: Int = 1) {
        val count = 10
        val groups = bot?.groups
        if (groups != null && groups.isNotEmpty()){
            val gl = groups.filterIndexed { index, _ -> index >= (page-1)*count && index < (page-1)*count+count }
            sendMessage(buildString {
                gl.forEach { appendLine("${it.id}@${it.name}") }
                appendLine("第 $page 页")
                appendLine("共 ${groups.count()} 个群")
            })
        }else{
            sendMessage("Bot还没有加群哦")
        }
    }

    @SubCommand("test")
    suspend fun CommandSenderOnMessage<FriendMessageEvent>.test(user:String, perm:String) {
        try {
            PermissionService.INSTANCE.getRegisteredPermissions().forEach {
                if (it.id == PermissionId.parseFromString(perm)){
                    AbstractPermitteeId.parseFromString(user).permit(it)
                    sendMessage("成功")
                    return
                }
            }
            sendMessage("未找到")
        }catch (e:Exception){
            println(e.message)
        }
    }

}

