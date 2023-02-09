package top.colter.mirai.plugin.welcome

import net.mamoe.mirai.console.data.ReadOnlyPluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object WelcomePluginConfig: ReadOnlyPluginConfig("WelcomePluginConfig"){

    @ValueDescription("管理员")
    val admin: Long by value(3375582524)

    @ValueDescription("好友申请\n0: 仅发送通知\n1: 自动拒绝\n2: 自动同意\n3: 管理员审核")
    val newFriendRequest: Int by value(3)

    @ValueDescription("邀请Bot加群申请\n0: 仅发送通知\n1: 自动拒绝\n2: 自动同意\n3: 管理员审核")
    val botInvitedJoinGroupRequest: Int by value(3)

    @ValueDescription("好友请求欢迎语\n如不需要请填写 '' 两个单引号")
    val friendWelcomeMessage: String by value("( •̀ ω •́ )✧")

    @ValueDescription("新成员加群欢迎语(需要授权)\n如不需要请填写 '' 两个单引号\n${'$'}{群名}替换被加群的群名，${'$'}{用户名}替换新加群员的昵称，${'$'}{头像}替换新加群员的头像\n注：暂时不支持取消关键词替换")
    val groupWelcomeMessage: String by value("${'$'}{用户名}，欢迎入群( •̀ ω •́ )✧")


}