package code.blurone.securitypl3

import code.blurone.securitypl3.commands.ChangePasswordExecutor
import code.blurone.securitypl3.commands.ForceChangePasswordExecutor
import code.blurone.securitypl3.commands.ForceLogoutExecutor
import code.blurone.securitypl3.commands.LogoutExecutor
import code.blurone.securitypl3.events.PlayerUnauthorizedEvent
import com.google.common.base.Charsets
import com.google.common.hash.Hashing
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.GameMode
import org.bukkit.NamespacedKey
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarFlag
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.server.BroadcastMessageEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import java.io.File
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
    private val useTheChatTitle = config.getBoolean("use_the_chat_title", true)
    private val unauthorizedInventories = mutableMapOf<String, Array<ItemStack>>()

    override fun onEnable() {
        // Plugin startup logic
        saveDefaultConfig()
        saveResource("translations/default.yml", true)
        supportedTranslations.forEach {
            if (!File(dataFolder, "translations/$it.yml").exists())
                saveResource("translations/$it.yml", false)
        }
        for (file in dataFolder.resolve("translations").listFiles()!!) {
            if (file.extension != "yml") continue

            translations[file.nameWithoutExtension.lowercase()] = try {
                val yaml = YamlConfiguration()
                yaml.load(file)
                yaml
            } catch (e: Exception) {
                logger.warning("Failed to load ${file.name}.")
                continue
            }
        }
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

    // 🔒 🔓 🔑 👤 » 🔐

    @EventHandler
    private fun onPlayerJoin(event: PlayerJoinEvent)
    {
        val savedAddress = event.player.persistentDataContainer.get(addressNamespacedKey, PersistentDataType.LONG)
        val hashedAddress = Hashing.sha256().hashBytes(event.player.address!!.address.address).asLong()
        if (savedAddress != hashedAddress)
            event.player.persistentDataContainer.set(authorizedNamespacedKey, PersistentDataType.BOOLEAN, false)
        event.player.persistentDataContainer.set(addressNamespacedKey, PersistentDataType.LONG, hashedAddress)

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
        unauthorizedInventories.remove(event.player.name)?.let {
            event.player.inventory.contents = it
        }
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

        val hasPassword = event.player.persistentDataContainer.has(passwordNamespacedKey, PersistentDataType.LONG)

        event.player.spigot().sendMessage(
            *ComponentBuilder(if (hasPassword) "\uD83D\uDD12" else "\uD83D\uDD11").color(ChatColor.RED)
                .append(" » ").color(ChatColor.GRAY)
                .append(getTranslation(if (hasPassword) "request_password" else "request_new_password", event.player.locale)).reset()
                .create()
        )

        unauthorizedInventories[event.player.name] = event.player.inventory.contents
        event.player.inventory.clear()

        if (useTheChatTitle)
            event.player.sendTitle("", getTranslation("use_the_chat", event.player.locale), 10, 12000, 10)

        if (!hasPassword)
            return

        createBossBar(event.player)
        event.player.persistentDataContainer.set(attemptsNamespacedKey, PersistentDataType.INTEGER, maxAttempts)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onBroadcastMessageEvent(event: BroadcastMessageEvent)
    {
        event.recipients.removeIf { it is Player && it.persistentDataContainer.get(authorizedNamespacedKey, PersistentDataType.BOOLEAN) != true }
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
                        handleLogin(event.player, "\uD83D\uDD13")
                    }
                }.runTask(this)

            return
        }

        val tempPassword = event.player.persistentDataContainer.get(tempPasswordNamespacedKey, PersistentDataType.LONG)
            ?: run {
                event.player.spigot().sendMessage(
                    *ComponentBuilder("\uD83D\uDD11").color(ChatColor.GOLD)
                        .append(" » ").color(ChatColor.GRAY)
                        .append(getTranslation("repeat_password", event.player.locale)).reset()
                        .create()
                )
                return event.player.persistentDataContainer.set(tempPasswordNamespacedKey, PersistentDataType.LONG, password)
            }

        event.player.persistentDataContainer.remove(tempPasswordNamespacedKey)

        if (password != tempPassword)
            return event.player.spigot().sendMessage(
                *ComponentBuilder("\uD83D\uDD11").color(ChatColor.RED)
                    .append(" » ").color(ChatColor.GRAY)
                    .append(getTranslation("passwords_dont_match", event.player.locale)).reset()
                    .create()
            )

        event.player.persistentDataContainer.set(passwordNamespacedKey, PersistentDataType.LONG, password)
        object : BukkitRunnable() {
            override fun run() {
                handleLogin(event.player, "\uD83D\uDD11")
            }
        }.runTask(this)
    }

    private fun handleLogin(player: Player, icon: String)
    {
        player.persistentDataContainer.set(authorizedNamespacedKey, PersistentDataType.BOOLEAN, true)
        player.spigot().sendMessage(
            *ComponentBuilder(icon).color(ChatColor.GREEN)
                .append(" » ").color(ChatColor.GRAY)
                .append(getTranslation("authenticated", player.locale)).reset()
                .create()
        )
        unauthorizedInventories.remove(player.name)?.let {
            player.inventory.contents = it
        }
        if (useTheChatTitle)
            player.resetTitle()
        val namespacedKey = NamespacedKey(this, "attempts_for_${player.name}")
        server.getBossBar(namespacedKey)?.removeAll()
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
        player.spigot().sendMessage(
            *ComponentBuilder("\uD83D\uDD12").color(ChatColor.RED)
                .append(" » ").color(ChatColor.GRAY)
                .append(getTranslation("wrong_password", player.locale)).reset()
                .create()
        )
        var attempts = player.persistentDataContainer.get(attemptsNamespacedKey, PersistentDataType.INTEGER)!!
        player.persistentDataContainer.set(attemptsNamespacedKey, PersistentDataType.INTEGER, --attempts)
        val namespacedKey = NamespacedKey(this, "attempts_for_${player.name}")
        val bossBar = server.getBossBar(namespacedKey) ?: createBossBar(player)
        bossBar.progress = attemptFactor * attempts

        if (attempts > 0)
            return

        bossBar.removeAll()
        server.removeBossBar(namespacedKey)
        player.banIp(getTranslation("too_many_attempts", player.locale), banDuration, null, true)
    }

    private fun createBossBar(player: Player): BossBar
    {
        val namespacedKey = NamespacedKey(this, "attempts_for_${player.name}")
        val bossBar = server.getBossBar(namespacedKey) ?:
            server.createBossBar(namespacedKey, getTranslation("attempts", player.locale), BarColor.RED, BarStyle.SOLID, BarFlag.CREATE_FOG, BarFlag.DARKEN_SKY)
        bossBar.addPlayer(player)
        bossBar.progress = 1.0
        return bossBar
    }

    companion object
    {
        private val supportedTranslations = listOf("en", "es")
        private val translations = mutableMapOf<String, YamlConfiguration>()

        fun getTranslation(key: String, locale: String): String
        {
            return translations[locale.lowercase()]?.getString(key) ?:
            translations[locale.split('_')[0].lowercase()]?.getString(key) ?:
            translations["default"]?.getString(key) ?:
            key
        }
    }
}
