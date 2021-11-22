package top.colter.mirai.plugin.welcome

import net.mamoe.mirai.console.data.ReadOnlyPluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object WelcomePluginConfig: ReadOnlyPluginConfig("WelcomePluginConfig"){

    @ValueDescription("自动同意好友申请 true or false")
    val agreeNewFriendRequest: Boolean by value(false)

    @ValueDescription("好友请求欢迎语 如不需要请填写 \"\" 双引号")
    val friendWelcomeMessage: String by value("( •̀ ω •́ )✧")

    @ValueDescription("新成员加群欢迎语 如不需要请填写 \"\" 双引号")
    val groupWelcomeMessage: String by value("欢迎( •̀ ω •́ )✧")


}