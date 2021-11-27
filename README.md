# SimpleWelcomePlugin

> [Mirai 插件](https://github.com/mamoe/mirai)

## 简介
进群欢迎、管理Bot好友/群请求

当Bot有好友请求或者邀请bot进群，就会给管理员发送消息，管理员进行处理    
可以自动处理

## 下载
前往 [releases](https://github.com/Colter23/simple-welcome-plugin/releases) 下载并放到 `plugin` 文件夹中

## 配置
```yaml
# 管理员QQ号(只能是QQ号)
admin: 11111111

# 好友申请
# 0: 仅发送通知
# 1: 自动拒绝
# 2: 自动同意
# 3: 管理员审核
newFriendRequest: 3

# 邀请Bot加群申请
# 0: 仅发送通知
# 1: 自动拒绝
# 2: 自动同意
# 3: 管理员审核
botInvitedJoinGroupRequest: 3

# 新好友欢迎语(目前仅bot自动同意才会发送)
# 如不需要请填写 '' 两个单引号
friendWelcomeMessage: ''

# 新成员加群欢迎语(需要授权, 后文有说明) 
# 如不需要请填写 '' 两个单引号
groupWelcomeMessage: ''

```

## 使用
对于新成员加群欢迎功能，授权 = 开启   
授权权限 `group:welcome.message`    
例:    
对所有群开启 `/perm add g* group:welcome.message`    
对某个群开启 `/perm add g111111111 group:welcome.message`

插件指令: 
```
/wcl <g group 群> [页]      # 分页获取bot所加的群
/wcl <f friend 好友> [页]   # 分页获取bot的好友

栗子: 
/wcl g         # 获取bot第一页群(每页10个)
/wcl group 2   # 获取bot第二页群(每页10个)
```
以上指令需要 [在聊天环境执行指令](https://github.com/project-mirai/chat-command) 插件    

对于开启了群欢迎功能的群，可以在群内使用 `#r` 获取一个随机骰子


