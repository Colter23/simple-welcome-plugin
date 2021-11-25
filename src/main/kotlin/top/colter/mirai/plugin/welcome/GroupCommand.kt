package top.colter.mirai.plugin.welcome

import net.mamoe.mirai.console.command.CommandSenderOnMessage
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.event.events.FriendMessageEvent

object GroupCommand : CompositeCommand(
    owner = PluginMain,
    "group"
) {

    @SubCommand("create", "创建")
    suspend fun CommandSenderOnMessage<FriendMessageEvent>.create(name: String) {
        if (this.fromEvent.sender.id == WelcomePluginConfig.admin) {
            WelcomePluginData.group[name] = mutableListOf()
            sendMessage("创建成功")
        }
    }

    @SubCommand("add", "添加")
    suspend fun CommandSenderOnMessage<FriendMessageEvent>.add(groupName: String, contacts: String) {
        if (this.fromEvent.sender.id == WelcomePluginConfig.admin) {
            val group = WelcomePluginData.group[groupName]
            if (group != null){
                group.addAll(contacts.split(",","，").map { it.toLong() })
                sendMessage("添加成功")
            }else{
                sendMessage("没有这个组哦")
            }
        }
    }

    @SubCommand("list", "列表")
    suspend fun CommandSenderOnMessage<FriendMessageEvent>.list(groupName: String = "") {
        if (this.fromEvent.sender.id == WelcomePluginConfig.admin) {
            if (groupName.isEmpty()) {
                sendMessage(buildString {
                    WelcomePluginData.group.keys.forEach { appendLine(it) }
                })
            } else {
                val group = WelcomePluginData.group[groupName]
                if (group != null) {
                    buildString {
                        appendLine("组: $groupName")
                        group.forEach { appendLine(it) }
                    }
                }else{
                    sendMessage("没有这个组哦")
                }
            }
        }
    }

}