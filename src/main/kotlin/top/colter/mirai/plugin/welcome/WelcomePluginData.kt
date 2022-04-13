package top.colter.mirai.plugin.welcome

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object WelcomePluginData : AutoSavePluginData("WelcomePluginData"){

    @ValueDescription("组")
    val group: MutableMap<String, MutableList<String>> by value(mutableMapOf())

    @ValueDescription("组权限")
    val groupPerm: MutableMap<String, MutableList<String>> by value(mutableMapOf())

    @ValueDescription("群欢迎信息")
    val groupWelcomeMessage: MutableMap<String, String> by value(mutableMapOf())

}