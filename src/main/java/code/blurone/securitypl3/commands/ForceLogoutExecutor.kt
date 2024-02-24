package code.blurone.securitypl3.commands

import code.blurone.securitypl3.SecurityPL3
import code.blurone.securitypl3.events.PlayerUnauthorizedEvent
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.command.RemoteConsoleCommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
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
            sender.spigot().sendMessage(
                *ComponentBuilder("\uD83D\uDC64").color(ChatColor.RED)
                    .append(" » ").color(ChatColor.GRAY)
                    .append(SecurityPL3.getTranslation("player_not_found",
                        if (sender is Player) sender.locale else "default"
                    )).reset()
                    .create()
            )
            return true
        }
        if (sender !is ConsoleCommandSender && sender !is RemoteConsoleCommandSender && !operatorVersus && player.isOp)
        {
            sender.spigot().sendMessage(
                *ComponentBuilder("\uD83D\uDC64").color(ChatColor.RED)
                    .append(" » ").color(ChatColor.GRAY)
                    .append(SecurityPL3.getTranslation("player_is_op",
                        if (sender is Player) sender.locale else "default"
                    )).reset()
                    .create()
            )
            return true
        }

        sender.spigot().sendMessage(
            *ComponentBuilder("\uD83D\uDC64").color(ChatColor.GREEN)
                .append(" » ").color(ChatColor.GRAY)
                .append(SecurityPL3.getTranslation("admin_forced_logout",
                    if (sender is Player) sender.locale else "default"
                )).reset()
                .create()
        )
        player.spigot().sendMessage(
            *ComponentBuilder("\uD83D\uDD12").color(ChatColor.BLUE)
                .append(" » ").color(ChatColor.GRAY)
                .append(SecurityPL3.getTranslation("player_forced_logout", player.locale)).reset()
                .create()
        )
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