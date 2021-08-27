package love.hana.bot.qq.safe

import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.ConsoleCommandSender

object Console: CompositeCommand(Plugin, "safe") {
    @SubCommand
    suspend fun ConsoleCommandSender.enable(groupID: Long) {
        Plugin.enable(groupID)
    }

    @SubCommand
    suspend fun ConsoleCommandSender.disable(groupID: Long) {
        Plugin.disable(groupID)
    }
}
