package top.colter.mirai.plugin.welcome

import kotlinx.coroutines.delay
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.info

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
        //配置文件目录 "${dataFolder.absolutePath}/"
        val eventChannel = GlobalEventChannel.parentScope(this)
//        eventChannel.subscribeAlways<GroupMessageEvent>{
//            //群消息
//            //复读示例
//            if (message.contentToString().startsWith("复读")) {
//                group.sendMessage(message.contentToString().replace("复读", ""))
//            }
//            if (message.contentToString() == "hi") {
//                //群内发送
//                group.sendMessage("hi")
//                //向发送者私聊发送消息
//                sender.sendMessage("hi")
//                //不继续处理
//                return@subscribeAlways
//            }
//            //分类示例
//            message.forEach {
//                //循环每个元素在消息里
//                if (it is Image) {
//                    //如果消息这一部分是图片
//                    val url = it.queryUrl()
//                    group.sendMessage("图片，下载地址$url")
//                }
//                if (it is PlainText) {
//                    //如果消息这一部分是纯文本
//                    group.sendMessage("纯文本，内容:${it.content}")
//                }
//            }
//        }
        eventChannel.subscribeAlways<FriendMessageEvent>{
            //好友信息
//            sender.sendMessage("hi")
        }
        eventChannel.subscribeAlways<NewFriendRequestEvent>{
            //自动同意好友申请
            if(WelcomePluginConfig.agreeNewFriendRequest){
                accept()
                if (WelcomePluginConfig.friendWelcomeMessage.isNotEmpty()){
                    delay(2000)
                    bot.getFriend(fromId)?.sendMessage(WelcomePluginConfig.friendWelcomeMessage)
                }
            }

        }
        eventChannel.subscribeAlways<MemberJoinEvent>{
            if(WelcomePluginConfig.groupWelcomeMessage.isNotEmpty()){
                group.sendMessage(At(user) +" "+WelcomePluginConfig.groupWelcomeMessage)
            }

        }
        eventChannel.subscribeAlways<BotInvitedJoinGroupRequestEvent>{
            //自动同意加群申请
//            accept()
        }
    }
}
