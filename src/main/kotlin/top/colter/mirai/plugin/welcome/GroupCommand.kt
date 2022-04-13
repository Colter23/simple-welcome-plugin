package top.colter.mirai.plugin.welcome

import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.permission.AbstractPermitteeId
import net.mamoe.mirai.console.permission.PermissionId
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.permission.PermissionService.Companion.cancel
import net.mamoe.mirai.console.permission.PermissionService.Companion.permit

object GroupCommand : CompositeCommand(
    owner = PluginMain,
    "group"
) {

    @SubCommand("create", "创建")
    suspend fun CommandSender.create(name: String) {
        if (WelcomePluginData.group[name] != null){
            sendMessage("$name 组已存在")
        }else{
            WelcomePluginData.group[name] = mutableListOf()
            sendMessage("创建成功")
        }
    }

    @SubCommand("del", "删除")
    suspend fun CommandSender.del(name: String) {
        //TODO 删除组前确认
        if (WelcomePluginData.group[name] != null){
            WelcomePluginData.group.remove(name)
            WelcomePluginData.groupPerm.remove(name)
            sendMessage("$name 组删除成功")
        }else{
            sendMessage("没有这个组哦")
        }
    }

    @SubCommand("add", "添加")
    suspend fun CommandSender.add(groupName: String, contacts: String) {
        val group = WelcomePluginData.group[groupName]
        if (group != null){
            var failMsg = ""
            group.addAll(contacts.split(",","，").map {
                val c = formatContact(it)
                if (c == "") failMsg += "$it, "
                return@map c
            }.filter { it.isNotEmpty() })
            val perm = WelcomePluginData.groupPerm[groupName]?.map { PermissionId.parseFromString(it) }
            if (perm != null){
                PermissionService.INSTANCE.getRegisteredPermissions().forEach {p ->
                    if (perm.contains(p.id)){
                        WelcomePluginData.group.filterKeys { g -> g == groupName }.values.forEach { c ->
                            c.forEach { v -> AbstractPermitteeId.parseFromString(v).permit(p) }
                        }
                    }
                }
            }

            if (failMsg.isEmpty()){
                sendMessage("添加成功")
            }else{
                sendMessage("$failMsg 未找到")
            }
        }else{
            sendMessage("没有这个组哦")
        }
    }

    @SubCommand("ban")
    suspend fun CommandSender.ban(groupName: String, contacts: String) {
        val group = WelcomePluginData.group[groupName]
        if (group != null){
            var failMsg = ""
            val conList = contacts.split(",","，").map {
                val c = formatContact(it)
                if (c == "") failMsg += "$it, "
                return@map c
            }.filter { it.isNotEmpty() }
            group.removeAll(conList)
            WelcomePluginData.groupPerm[groupName]?.forEach { perm ->
                PermissionService.INSTANCE.getRegisteredPermissions().forEach {
                    if (it.id == PermissionId.parseFromString(perm)){
                        conList.forEach { c ->
                            AbstractPermitteeId.parseFromString(c).cancel(it, false)
                        }
                    }
                }
            }
            if (failMsg.isEmpty()){
                sendMessage("删除成功")
            }else{
                sendMessage("$failMsg 未找到")
            }
        }else{
            sendMessage("没有这个组哦")
        }
    }

    @SubCommand("list", "列表")
    suspend fun CommandSender.list(groupName: String = "") {
        if (groupName.isEmpty()) {
            sendMessage(buildString {
                WelcomePluginData.group.keys.forEach { appendLine(it) }
                appendLine("输入具体的组名查看组成员和权限")
            })
        } else {
            val group = WelcomePluginData.group[groupName]
            if (group != null) {
                sendMessage(buildString {
                    appendLine("组: $groupName")
                    group.forEach { appendLine(it) }
                    WelcomePluginData.groupPerm[groupName]?.forEach {
                        appendLine(it)
                    }
                })
            }else{
                sendMessage("没有这个组哦")
            }
        }
    }

    @SubCommand("perm", "授权")
    suspend fun CommandSender.perm(groupName: String, permStr: String) {
        val group = WelcomePluginData.group[groupName]
        if (group != null){
            val perm: PermissionId
            try {
                perm = PermissionId.parseFromString(permStr)
            }catch (e: Exception){
                sendMessage("权限错误, 请用 /perm lp 查看所有权限")
                return
            }
            PermissionService.INSTANCE.getRegisteredPermissions().forEach {
                if (it.id == perm){
                    WelcomePluginData.group.filterKeys { g -> g == groupName }.values.forEach { c ->
                        c.forEach { v -> AbstractPermitteeId.parseFromString(v).permit(it) }
                    }
                    if (WelcomePluginData.groupPerm[groupName] == null){
                        WelcomePluginData.groupPerm[groupName] = mutableListOf(permStr)
                    }else{
                        WelcomePluginData.groupPerm[groupName]?.add(permStr)
                    }
                    sendMessage("添加成功")
                    return
                }
            }
            sendMessage("未找到此权限, 请用 /perm lp 查看所有权限")
        }else{
            sendMessage("没有这个组哦")
        }
    }

    @SubCommand("denyPerm", "取消授权")
    suspend fun CommandSender.denyPerm(groupName: String, permStr: String) {
        val group = WelcomePluginData.group[groupName]
        if (group != null){
            val perm: PermissionId
            try {
                perm = PermissionId.parseFromString(permStr)
            }catch (e: Exception){
                sendMessage("权限错误, 请用 /perm lp 查看所有权限")
                return
            }
            if (WelcomePluginData.groupPerm[groupName]?.contains(permStr) == true){
                PermissionService.INSTANCE.getRegisteredPermissions().forEach {
                    if (it.id == perm){
                        WelcomePluginData.group.filterKeys { g -> g == groupName }.values.forEach { c ->
                            c.forEach { v -> AbstractPermitteeId.parseFromString(v).cancel(it, false) }
                        }
                        WelcomePluginData.groupPerm[groupName]?.remove(permStr)
                        sendMessage("取消授权成功")
                        return
                    }
                }
            }
            sendMessage("没有此权限哦")
        }else{
            sendMessage("没有这个组哦")
        }
    }

    fun formatContact(del: String): String{
        if (del.isBlank()) return ""
        val delegate = del.toLong()
        for (bot in Bot.instances) {
            for (group in bot.groups) {
                if (group.id == delegate) return "g$del"
            }
            for (friend in bot.friends) {
                if (friend.id == delegate) return "u$del"
            }
            for (group in bot.groups) {
                for (member in group.members) {
                    if (member.id == delegate) return "m${group.id}.$del"
                }
            }
        }
        return ""
    }

}