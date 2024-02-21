package code.blurone.securitypl3

import code.blurone.securitypl3.commands.*
import code.blurone.securitypl3.events.PlayerUnauthorizedEvent
import com.google.common.base.Charsets
import com.google.common.hash.Hashing
import org.bukkit.GameMode
import org.bukkit.NamespacedKey
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarFlag
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import java.time.Duration

class SecurityPL3 : JavaPlugin(), Listener {
    private val addressNamespacedKey = NamespacedKey(this, "address")
    private val passwordNamespacedKey = NamespacedKey(this, "password")
    private val tempPasswordNamespacedKey = NamespacedKey(this, "temp_password")
    private val authorizedNamespacedKey = NamespacedKey(this, "authorized")
    private val attemptsNamespacedKey = NamespacedKey(this, "attempts")
    private val lastPosNamespacedKey = NamespacedKey(this, "last_pos")
    private val lastGameModeNamespacedKey = NamespacedKey(this, "last_gamemode")
    private val maxAttempts = config.getInt("password_attempts", 3).let { if (it < 1) 3 else it}
    private val attemptFactor = 1.0 / maxAttempts
    private val defaultLocation = config.getLocation("unauthorized_location")
    private val banDuration = config.getLong("ban_duration", 300).let { if (it < 0) null else Duration.ofSeconds(it) }

    override fun onEnable() {
        // Plugin startup logic
        saveDefaultConfig()
        server.pluginManager.let {
            it.registerEvents(this, this)
            it.registerEvents(CancelEventsListener(authorizedNamespacedKey), this)
        }
        getCommand("logout")?.setExecutor(LogoutExecutor(authorizedNamespacedKey))
        getCommand("changepassword")?.setExecutor(ChangePasswordExecutor(authorizedNamespacedKey, tempPasswordNamespacedKey))
        val operaturVersus = config.getBoolean("op_vs_op", false)
        getCommand("forcelogout")?.setExecutor(ForceLogoutExecutor(authorizedNamespacedKey, operaturVersus))
        getCommand("forcechangepassword")?.setExecutor(ForceChangePasswordExecutor(authorizedNamespacedKey, passwordNamespacedKey, operaturVersus))
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    private fun onPlayerJoin(event: PlayerJoinEvent)
    {
        val savedAddress = event.player.persistentDataContainer.get(addressNamespacedKey, PersistentDataType.LONG)
        val hashedAddress = Hashing.sha256().hashBytes(event.player.address!!.address.address).asLong()
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

    @EventHandler
    private fun onPlayerQuit(event: PlayerQuitEvent)
    {
        event.player.persistentDataContainer.remove(tempPasswordNamespacedKey)
    }

    @EventHandler
    private fun unauthorizedHandler(event: PlayerUnauthorizedEvent)
    {
        if (!event.player.persistentDataContainer.has(lastPosNamespacedKey, LocationDataType()))
            event.player.persistentDataContainer.set(lastPosNamespacedKey, LocationDataType(), event.player.location)

        if (!event.player.persistentDataContainer.has(lastGameModeNamespacedKey, PersistentDataType.STRING))
            event.player.persistentDataContainer.set(lastGameModeNamespacedKey, PersistentDataType.STRING, event.player.gameMode.name)

        event.player.teleport(defaultLocation ?: server.worlds[0].spawnLocation)
        event.player.gameMode = GameMode.SPECTATOR

        event.player.sendMessage("Please, enter ${
            if (event.player.persistentDataContainer.has(passwordNamespacedKey, PersistentDataType.LONG))
                "your"
            else
                "a new"
        } password.")

        if (!event.player.persistentDataContainer.has(passwordNamespacedKey, PersistentDataType.LONG))
            return

        createBossBar(event.player)
        event.player.persistentDataContainer.set(attemptsNamespacedKey, PersistentDataType.INTEGER, maxAttempts)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onChat(event: AsyncPlayerChatEvent)
    {
        event.recipients.removeIf { it.persistentDataContainer.get(authorizedNamespacedKey, PersistentDataType.BOOLEAN) != true }

        if (event.player.persistentDataContainer.get(authorizedNamespacedKey, PersistentDataType.BOOLEAN) == true)
            return

        event.isCancelled = true

        val password = Hashing.sha256().hashString(event.message, Charsets.UTF_8).asLong()
        event.player.persistentDataContainer.get(passwordNamespacedKey, PersistentDataType.LONG)?.let {
            if (password != it)
            {
                object : BukkitRunnable() {
                    override fun run() {
                        handleWrongAttempt(event.player)
                    }
                }.runTask(this)
                return
            }

            if (event.player.persistentDataContainer.has(tempPasswordNamespacedKey, PersistentDataType.LONG))
                object : BukkitRunnable() {
                    override fun run() {
                        handleChangePassword(event.player)
                    }
                }.runTask(this)
            else
                object : BukkitRunnable() {
                    override fun run() {
                        handleLogin(event.player)
                    }
                }.runTask(this)

            return
        }

        val tempPassword = event.player.persistentDataContainer.get(tempPasswordNamespacedKey, PersistentDataType.LONG)
            ?: run {
                event.player.sendMessage("Repeat the password.")
                return event.player.persistentDataContainer.set(tempPasswordNamespacedKey, PersistentDataType.LONG, password)
            }

        event.player.persistentDataContainer.remove(tempPasswordNamespacedKey)

        if (password != tempPassword)
            return event.player.sendMessage("Passwords do not match.")

        event.player.persistentDataContainer.set(passwordNamespacedKey, PersistentDataType.LONG, password)
        object : BukkitRunnable() {
            override fun run() {
                handleLogin(event.player)
            }
        }.runTask(this)
    }

    private fun handleLogin(player: Player)
    {
        player.persistentDataContainer.set(authorizedNamespacedKey, PersistentDataType.BOOLEAN, true)
        player.sendMessage("You are now authenticated.")
        val namespacedKey = NamespacedKey(this, "attempts_for_${player.name}")
        server.getBossBar(namespacedKey)!!.removeAll()
        server.removeBossBar(namespacedKey)
        player.teleport(player.persistentDataContainer.get(lastPosNamespacedKey, LocationDataType()) ?: defaultLocation ?: server.worlds[0].spawnLocation)
        player.persistentDataContainer.remove(lastPosNamespacedKey)
        player.gameMode = player.persistentDataContainer.get(lastGameModeNamespacedKey, PersistentDataType.STRING)?.let{
            try { GameMode.valueOf(it) } catch (e: IllegalArgumentException) { null }
        } ?: server.defaultGameMode
        player.persistentDataContainer.remove(lastGameModeNamespacedKey)
    }

    private fun handleChangePassword(player: Player)
    {
        player.persistentDataContainer.remove(tempPasswordNamespacedKey)
        player.persistentDataContainer.remove(passwordNamespacedKey)
        server.pluginManager.callEvent(PlayerUnauthorizedEvent(player))
    }

    private fun handleWrongAttempt(player: Player)
    {
        player.sendMessage("Wrong password.")
        var attempts = player.persistentDataContainer.get(attemptsNamespacedKey, PersistentDataType.INTEGER)!!
        player.persistentDataContainer.set(attemptsNamespacedKey, PersistentDataType.INTEGER, --attempts)
        val namespacedKey = NamespacedKey(this, "attempts_for_${player.name}")
        val bossBar = server.getBossBar(namespacedKey) ?: createBossBar(player)
        bossBar.progress = attemptFactor * attempts

        if (attempts > 0)
            return

        bossBar.removeAll()
        server.removeBossBar(namespacedKey)
        player.banIp("Too many wrong attempts.", banDuration, null, true)
    }

    private fun createBossBar(player: Player): BossBar
    {
        val namespacedKey = NamespacedKey(this, "attempts_for_${player.name}")
        val bossBar = server.getBossBar(namespacedKey) ?:
            server.createBossBar(namespacedKey, "Attempts", BarColor.RED, BarStyle.SOLID, BarFlag.CREATE_FOG, BarFlag.DARKEN_SKY)
        bossBar.addPlayer(player)
        bossBar.progress = 1.0
        return bossBar
    }
}
