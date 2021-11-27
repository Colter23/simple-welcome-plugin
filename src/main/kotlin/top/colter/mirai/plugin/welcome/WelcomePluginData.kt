package top.colter.mirai.plugin.welcome

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object WelcomePluginData : AutoSavePluginData("WelcomePluginData"){

    @ValueDescription("群组 暂时没用")
    val group: MutableMap<String, MutableList<Long>> by value(mutableMapOf())

}