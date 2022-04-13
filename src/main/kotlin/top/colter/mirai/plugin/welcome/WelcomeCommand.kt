package top.colter.mirai.plugin.welcome

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.ContactList
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Group
import kotlin.math.ceil

object WelcomeCommand : CompositeCommand(
    owner = PluginMain,
    "wlc"
) {

    @SubCommand("group", "g","群")
    suspend fun CommandSender.group(page: Int = 1) = sendMessage(
        bot?.groups.pageQuery(page, 10)
    )

    @SubCommand("friend", "f","好友")
    suspend fun CommandSender.friend(page: Int = 1) = sendMessage(
        bot?.friends.pageQuery(page, 10)
    )

    @SubCommand("msg")
    suspend fun CommandSender.msg(group: String, msg: String){
        if (WelcomePluginData.group.keys.contains(group)){
            WelcomePluginData.groupWelcomeMessage[group] = msg
        }else{
            try {
                if (bot?.groups?.contains(group.toLong()) == true){
                    WelcomePluginData.groupWelcomeMessage[group] = msg
                    sendMessage("设置成功")
                }else{
                    sendMessage("设置失败")
                }
            }catch (e: Exception){
                sendMessage("设置失败")
            }

        }
    }

//    @SubCommand("test")
//    suspend fun CommandSenderOnMessage<FriendMessageEvent>.test(user:String, perm:String) {
//        try {
//            PermissionService.INSTANCE.getRegisteredPermissions().forEach {
//                if (it.id == PermissionId.parseFromString(perm)){
//                    AbstractPermitteeId.parseFromString(user).permit(it)
//                    sendMessage("成功")
//                    return
//                }
//            }
//            sendMessage("未找到")
//        }catch (e:Exception){
//            println(e.message)
//        }
//    }

    private fun ContactList<Contact>?.pageQuery(page: Int, pageCount:Int): String {
        return if (this != null && this.isNotEmpty()){
            val pageSize = ceil(this.size.toDouble() / pageCount).toInt()
            if(page < 1 || page > pageSize){
                "页码超出范围（1 到 $pageSize）"
            }else{
                val isGroup = this.first() is Group
                val gl = this.filterIndexed { index, _ -> index >= (page-1)*pageCount && index < (page-1)*pageCount+pageCount }
                buildString {
                    gl.forEach { appendLine("${it.id}@${if(isGroup) (it as Group).name else (it as Friend).nick}") }
                    appendLine("第 $page 页, 共 $pageSize 页")
                    appendLine("共 $size 个${if(isGroup) "群" else "好友"}")
                }
            }
        }else{
            "空"
        }
    }

}
