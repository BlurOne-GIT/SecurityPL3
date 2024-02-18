package code.blurone.securitypl3.commands

import code.blurone.securitypl3.events.PlayerUnauthorizedEvent
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

class LogoutExecutor(private val authorizedNamespacedKey: NamespacedKey) : TabExecutor {
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

        sender.persistentDataContainer.set(authorizedNamespacedKey, PersistentDataType.BOOLEAN, false)
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