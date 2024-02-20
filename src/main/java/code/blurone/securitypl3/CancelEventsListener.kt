package code.blurone.securitypl3

import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityEvent
import org.bukkit.event.player.*
import org.bukkit.persistence.PersistentDataType

class CancelEventsListener(private val authorizedNamespacedKey: NamespacedKey) : Listener {
    private fun <T> cancelPlayerEvent(event: T) where T : Cancellable, T : PlayerEvent {
        if (event.player.persistentDataContainer.get(authorizedNamespacedKey, PersistentDataType.BOOLEAN) == true)
            return

        event.isCancelled = true
    }

    private fun <T> cancelEntityEvent(event: T) where T : Cancellable, T : EntityEvent {
        if (event.entity is Player && event.entity.persistentDataContainer.get(authorizedNamespacedKey, PersistentDataType.BOOLEAN) != true)
            return

        event.isCancelled = true
    }

    // org.bukkit.event.player package

    // Asyncs ignored

    // PlayerAdvancementDoneEvent (non Cancellable)

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onPlayerAnimationEvent(event: PlayerAnimationEvent) {
        cancelPlayerEvent(event)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onPlayerArmorStandManipulateEvent(event: PlayerArmorStandManipulateEvent) {
        cancelPlayerEvent(event)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onPlayerBedEnterEvent(event: PlayerBedEnterEvent) {
        cancelPlayerEvent(event)
    }

    // PlayerBedLeaveEvent (not needed)

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onPlayerBucketEmptyEvent(event: PlayerBucketEmptyEvent) {
        cancelPlayerEvent(event)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onPlayerBucketEntityEvent(event: PlayerBucketEntityEvent) {
        cancelPlayerEvent(event)
    }

    // PlayerBucketEvent (Abstract)

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onPlayerBucketFillEvent(event: PlayerBucketFillEvent) {
        cancelPlayerEvent(event)
    }

    // PlayerBucketFishEvent (Deprecated)

    // PlayerChangedMainHandEvent (non Cancellable)

    // PlayerChangedWorldEvent (non Cancellable)

    // PlayerChannelEvent (Abstract)

    // PlayerChatEvent (Deprecated)

    // PlayerChatTabCompleteEvent (Deprecated)

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onPlayerCommandPreprocessEvent(event: PlayerCommandPreprocessEvent) {
        cancelPlayerEvent(event)
    }

    // PlayerCommandSendEvent (non Cancellable)

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onPlayerDropItemEvent(event: PlayerDropItemEvent) {
        cancelPlayerEvent(event)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onPlayerEditBookEvent(event: PlayerEditBookEvent) {
        cancelPlayerEvent(event)
    }

    // PlayerEggThrowEvent (non Cancellable)

    // PlayerEvent (Abstract (Static getHandlerList method required!))

    // PlayerExpChangeEvent (non Cancellable)

    // PlayerExpCooldownChangeEvent (non Cancellable)

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onPlayerFishEvent(event: PlayerFishEvent) {
        cancelPlayerEvent(event)
    }

    // PlayerGameModeChangeEvent (handled differently)

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onPlayerHarvestBlockEvent(event: PlayerHarvestBlockEvent) {
        cancelPlayerEvent(event)
    }

    // PlayerHideEntityEvent (not needed)

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onPlayerInteractAtEntityEvent(event: PlayerInteractAtEntityEvent) {
        cancelPlayerEvent(event)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onPlayerInteractEntityEvent(event: PlayerInteractEntityEvent) {
        cancelPlayerEvent(event)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onPlayerInteractEvent(event: PlayerInteractEvent) {
        cancelPlayerEvent(event)
    }

    // PlayerItemBreakEvent (non Cancellable)

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onPlayerItemConsumeEvent(event: PlayerItemConsumeEvent) {
        cancelPlayerEvent(event)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onPlayerItemDamageEvent(event: PlayerItemDamageEvent) {
        cancelPlayerEvent(event)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onPlayerItemHeldEvent(event: PlayerItemHeldEvent) {
        cancelPlayerEvent(event)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onPlayerItemMendEvent(event: PlayerItemMendEvent) {
        cancelPlayerEvent(event)
    }

    // PlayerJoinEvent (non Cancellable)

    // PlayerKickEvent (not needed)

    // PlayerLevelChangeEvent (non Cancellable)

    // PlayerLocaleChangeEvent (non Cancellable/not needed)

    // PlayerLoginEvent (non Cancellable/not needed)


    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onPlayerMoveEvent(event: PlayerMoveEvent) {
        if (event.from.distance(event.to!!) != 0.0)
            cancelPlayerEvent(event)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onPlayerPickupArrowEvent(event: PlayerPickupArrowEvent) {
        cancelPlayerEvent(event)
    }

    // PlayerPickupItemEvent (Deprecated)

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onPlayerPortalEvent(event: PlayerPortalEvent) {
        cancelPlayerEvent(event)
    }

    // PlayerPreLoginEvent (Deprecated)

    // PlayerQuitEvent (non Cancellable/not needed)

    // PlayerRecipeBookClickEvent (non Cancellable)

    // PlayerRecipeBookSettingsChangeEvent (non Cancellable/not needed)

    // PlayerRecipeDiscoverEvent (not needed)

    // PlayerRegisterChannelEvent (non Cancellable/not needed)

    // PlayerResourcePackStatusEvent (non Cancellable/not needed)

    // PlayerRespawnEvent (non Cancellable/not needed)

    // PlayerRiptideEvent (non Cancellable)

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onPlayerShearEntityEvent(event: PlayerShearEntityEvent) {
        cancelPlayerEvent(event)
    }

    // PlayerShowEntityEvent (not needed)

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onPlayerSignOpenEvent(event: PlayerSignOpenEvent) {
        cancelPlayerEvent(event)
    }

    // PlayerSpawnChangeEvent (not needed)

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onPlayerStatisticIncrementEvent(event: PlayerStatisticIncrementEvent) {
        cancelPlayerEvent(event)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onPlayerSwapHandItemsEvent(event: PlayerSwapHandItemsEvent) {
        cancelPlayerEvent(event)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onPlayerTakeLecternBookEvent(event: PlayerTakeLecternBookEvent) {
        cancelPlayerEvent(event)
    }

    // PlayerTeleportEvent (handled differently)

    // PlayerToggleFlightEvent (not needed)

    // PlayerToggleSneakEvent (not needed)

    // PlayerToggleSprintEvent (not needed)

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onPlayerUnleashEntityEvent(event: PlayerUnleashEntityEvent) {
        cancelEntityEvent(event)
    }

    // PlayerUnregisterChannelEvent (non Cancellable/not needed)

    // PlayerVelocityEvent (not needed)

    // org.bukkit.event.entity package
}