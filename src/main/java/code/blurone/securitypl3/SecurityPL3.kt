package code.blurone.securitypl3

import code.blurone.securitypl3.commands.LogoutExecutor
import code.blurone.securitypl3.events.PlayerUnauthorizedEvent
import com.google.common.hash.Hashing
import org.bukkit.NamespacedKey
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarFlag
import org.bukkit.boss.BarStyle
import org.bukkit.event.Cancellable
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerEvent
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin

class SecurityPL3 : JavaPlugin(), Listener {
    private val addressNamespacedKey = NamespacedKey(this, "address")
    private val passwordNamespacedKey = NamespacedKey(this, "password")
    private val authorizedNamespacedKey = NamespacedKey(this, "authorized")
    private val lastPosNamespacedKey = NamespacedKey(this, "last_pos")
    private val attemptFactor = 1.0 / config.getInt("password_attempts", 3)
    private val defaultLocation = config.getLocation("unauthorized_location")

    override fun onEnable() {
        // Plugin startup logic
        server.pluginManager.registerEvents(this, this)
        getCommand("logout")?.setExecutor(LogoutExecutor(authorizedNamespacedKey))
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    private fun onPlayerLogin(event: PlayerLoginEvent)
    {
        val savedAddress = event.player.persistentDataContainer.get(addressNamespacedKey, PersistentDataType.LONG)
        val hashedAddress = Hashing.sha256().hashBytes(event.realAddress.address).asLong()
        if (savedAddress == null)
            event.player.persistentDataContainer.set(addressNamespacedKey, PersistentDataType.LONG, hashedAddress)
        else if (savedAddress != hashedAddress)
            event.player.persistentDataContainer.set(authorizedNamespacedKey, PersistentDataType.BOOLEAN, false)

        // pre authorization checkpoint
        if (!event.player.persistentDataContainer.has(authorizedNamespacedKey, PersistentDataType.BOOLEAN) || !event.player.persistentDataContainer.has(passwordNamespacedKey, PersistentDataType.LONG))
            event.player.persistentDataContainer.set(authorizedNamespacedKey, PersistentDataType.BOOLEAN, false)

        // authorization checkpoint
        if (event.player.persistentDataContainer.get(authorizedNamespacedKey, PersistentDataType.BOOLEAN)!!)
            return

        server.pluginManager.callEvent(PlayerUnauthorizedEvent(event.player))
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun anyOtherPlayerEvent(event: PlayerEvent)
    {
        if (event !is Cancellable || event.isAsynchronous || event.player.persistentDataContainer.get(authorizedNamespacedKey, PersistentDataType.BOOLEAN) != true)
            return

        event.isCancelled = true
    }

    @EventHandler
    private fun unauthorizedHandler(event: PlayerUnauthorizedEvent)
    {
        if (!event.player.persistentDataContainer.has(lastPosNamespacedKey, LocationDataType()))
            event.player.persistentDataContainer.set(lastPosNamespacedKey, LocationDataType(), event.player.location)

        event.player.teleport(defaultLocation ?: server.worlds[0].spawnLocation)

        val bossBar = server.createBossBar(NamespacedKey(this, "attempts_for_${event.player.name}"), "Attempts", BarColor.RED, BarStyle.SOLID, BarFlag.CREATE_FOG, BarFlag.DARKEN_SKY)
        bossBar.addPlayer(event.player)
        bossBar.progress = 1.0
    }
}
