package code.blurone.securitypl3

import org.bukkit.NamespacedKey
import org.bukkit.event.Cancellable
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.*
import org.bukkit.persistence.PersistentDataType

class CancelEventsListener(private val authorizedNamespacedKey: NamespacedKey) : Listener {
    private fun <T> cancelPlayerEvent(event: T) where T : Cancellable, T : PlayerEvent {
        if (event.player.persistentDataContainer.get(authorizedNamespacedKey, PersistentDataType.BOOLEAN) != true)
            event.isCancelled = true
    }

    // org.bukkit.event.player package

    // Asyncs ignored

    // PlayerAdvancementDoneEvent (non Cancellable)

    // PlayerAnimationEvent (not needed)

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onPlayerArmorStandManipulateEvent(event: PlayerArmorStandManipulateEvent) {
        cancelPlayerEvent(event)
    }

    // PlayerBedEnterEvent (not needed)

    // PlayerBedLeaveEvent (not needed)

    // PlayerBucketEmptyEvent (not needed)

    // PlayerBucketEntityEvent (not needed)

    // PlayerBucketEvent (Abstract)

    // PlayerBucketFillEvent (not needed)

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

    // PlayerEditBookEvent (not needed)

    // PlayerEggThrowEvent (non Cancellable)

    // PlayerEvent (Abstract (Static getHandlerList method required!))

    // PlayerExpChangeEvent (non Cancellable)

    // PlayerExpCooldownChangeEvent (non Cancellable)

    // PlayerFishEvent (not needed)

    // PlayerGameModeChangeEvent (handled differently)

    // PlayerHarvestBlockEvent (not needed)

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

    // PlayerItemConsumeEvent (not needed)

    // PlayerItemDamageEvent (not needed)

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onPlayerItemHeldEvent(event: PlayerItemHeldEvent) {
        cancelPlayerEvent(event)
    }


    // PlayerItemMendEvent (not needed)

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

    // PlayerPickupArrowEvent (not needed)

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

    // PlayerShearEntityEvent (not needed)

    // PlayerShowEntityEvent (not needed)

    // PlayerSignOpenEvent (not needed)

    // PlayerSpawnChangeEvent (not needed)

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onPlayerStatisticIncrementEvent(event: PlayerStatisticIncrementEvent) {
        cancelPlayerEvent(event)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onPlayerSwapHandItemsEvent(event: PlayerSwapHandItemsEvent) {
        cancelPlayerEvent(event)
    }

    // PlayerTakeLecternBookEvent (not needed)

    // PlayerTeleportEvent (handled differently)

    // PlayerToggleFlightEvent (not needed)

    // PlayerToggleSneakEvent (not needed)

    // PlayerToggleSprintEvent (not needed)

    // PlayerUnleashEntityEvent (not needed)

    // PlayerUnregisterChannelEvent (non Cancellable/not needed)

    // PlayerVelocityEvent (not needed)

}