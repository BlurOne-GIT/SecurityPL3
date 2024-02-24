package code.blurone.securitypl3.commands

import code.blurone.securitypl3.SecurityPL3
import code.blurone.securitypl3.events.PlayerUnauthorizedEvent
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

class ChangePasswordExecutor(private val authorizedNamespacedKey: NamespacedKey, private val tempPassNamespacedKey: NamespacedKey) : TabExecutor {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (sender !is Player)
        {
            sender.sendMessage("You must be a player to use this command.")
            return true
        }

        sender.spigot().sendMessage(
            *ComponentBuilder("\uD83D\uDD10").color(ChatColor.GOLD)
                .append(" » ").color(ChatColor.GRAY)
                .append(SecurityPL3.getTranslation("pre_change_password", sender.locale)).reset()
                .create()
        )
        sender.persistentDataContainer.set(authorizedNamespacedKey, PersistentDataType.BOOLEAN, false)
        sender.persistentDataContainer.set(tempPassNamespacedKey, PersistentDataType.LONG, 0)

        Bukkit.getServer().pluginManager.callEvent(PlayerUnauthorizedEvent(sender))

        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): MutableList<String> {
        return mutableListOf()
    }
}