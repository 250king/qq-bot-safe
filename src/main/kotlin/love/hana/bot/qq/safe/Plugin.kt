package love.hana.bot.qq.safe

import com.mongodb.client.model.Filters
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.message.data.FlashImage
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import net.mamoe.mirai.message.data.PlainText

object Plugin: KotlinPlugin(JvmPluginDescription("fun.hana.bot.qq.safe", "2.0.0", "内容安全")) {
    private val CONFIG = ArrayList<Long>()

    fun enable(groupID: Long) {
        if (Kit.CONFIG.find(Filters.eq("groupID", groupID)).first() == null) {
            logger.error("The Group $groupID isn't existed!")
        }
        else {
            if (CONFIG.contains(groupID)) {
                logger.warning("The Group $groupID has been enabled!")
            }
            else {
                CONFIG.add(groupID)
                val new = Updates.combine(
                    Updates.set("enable", true)
                )
                val setting = UpdateOptions().upsert(true)
                Kit.CONFIG.updateOne(Filters.eq("groupID", groupID), new, setting)
            }
        }
    }

    fun disable(groupID: Long) {
        if (CONFIG.contains(groupID)) {
            CONFIG.remove(groupID)
            val new = Updates.combine(
                Updates.set("enable", false)
            )
            val setting = UpdateOptions().upsert(true)
            Kit.CONFIG.updateOne(Filters.eq("groupID", groupID), new, setting)
        }
        else {
            logger.warning("The Group $groupID has been disabled!")
        }
    }

    override fun onEnable() {
        CommandManager.registerCommand(Console)
        val groups = Kit.CONFIG.find()
        for (i in groups) {
            if (i.getBoolean("enable")) {
                CONFIG.add(i.getLong("groupID"))
            }
        }
        GlobalEventChannel.subscribeAlways<GroupMessageEvent> {event ->
            if (CONFIG.contains(event.group.id)) {
                try {
                    val images = ArrayList<Image>()
                    for (i in event.message) {
                        if (i is Image) {
                            images.add(i)
                        } else if (i is FlashImage) {
                            images.add(i.image)
                        }
                    }
                    if (images.size != 0) {
                        val result = Kit.check(images)
                        val illegal = ArrayList<Image>()
                        for (i in result.keys) {
                            if (result[i] == true) {
                                illegal.add(i)
                            }
                        }
                        if (illegal.size != 0) {
                            var message: MessageChain
                            if (event.sender.permission != MemberPermission.OWNER) {
                                if (event.sender.permission == MemberPermission.MEMBER) {
                                    event.sender.mute(604800)
                                    event.message.recall()
                                }
                                message = buildMessageChain {
                                    add(PlainText("哟！车开得挺快的！但这里不是飙车群，想开车还是劝你去别的群开。\n"))
                                    if (event.sender.permission == MemberPermission.MEMBER) {
                                        add(PlainText("照群主的意思，先让你吃一周的牢饭吧！如果不服的话，可以找群主评理（就看他愿不愿意咯）。\n"))
                                    } else {
                                        add(PlainText("原本照群主的意思，先让你吃一周的牢饭。但你是管理员，我没法整活你。但并不意味着你可以为所欲为呀！\n"))
                                    }
                                    add(PlainText("参考ID："))
                                    for (i in illegal) {
                                        add(PlainText("\n\t${i.imageId}"))
                                    }
                                }
                                event.sender.sendMessage(message)
                            }
                            message = buildMessageChain {
                                if (event.sender.permission == MemberPermission.ADMINISTRATOR) {
                                    add(PlainText("你哪怕你是群主，也不能那么放荡呀！\n"))
                                } else {
                                    add(PlainText("Hi！抓到一位开车的家伙，你看看怎么处理吧！\n"))
                                    add(PlainText("${event.sender.nick}（${event.sender.id}）\n"))
                                }
                                add(PlainText("参考ID："))
                                for (i in illegal) {
                                    add(PlainText("\n\t${i.imageId}"))
                                }
                            }
                            event.group.owner.sendMessage(message)
                        }
                    }
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
            }
        }
    }
}