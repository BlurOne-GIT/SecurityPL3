package code.blurone.securitypl3.commands

import code.blurone.securitypl3.events.PlayerUnauthorizedEvent
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.command.RemoteConsoleCommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.persistence.PersistentDataType

class ForceLogoutExecutor(private val authorizedNamespacedKey: NamespacedKey, private val operatorVersus: Boolean) : TabExecutor {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (!sender.isOp)
        {
            sender.sendMessage("You must be an operator to use this command.")
            return true
        }

        if (args.size != 1)
            return false

        val player = Bukkit.getPlayer(args[0])

        if (player == null)
        {
            sender.sendMessage("Player not found.")
            return true
        }
        if (sender !is ConsoleCommandSender && sender !is RemoteConsoleCommandSender && !operatorVersus && player.isOp)
        {
            sender.sendMessage("Player is an operator.")
            return true
        }

        player.persistentDataContainer.set(authorizedNamespacedKey, PersistentDataType.BOOLEAN, false)
        Bukkit.getServer().pluginManager.callEvent(PlayerUnauthorizedEvent(player))

        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): MutableList<String> {
        return if (args.size != 1)
            mutableListOf()
        else if (operatorVersus)
            Bukkit.getOnlinePlayers().map { it.name }.toMutableList()
        else
            Bukkit.getOnlinePlayers().filter { it.isOp }.map { it.name }.toMutableList()
    }
}