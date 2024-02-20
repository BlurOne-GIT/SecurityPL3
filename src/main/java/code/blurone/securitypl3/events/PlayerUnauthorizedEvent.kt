package code.blurone.securitypl3.events

import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

internal class PlayerUnauthorizedEvent(who: Player) : PlayerEvent(who) {
    companion object {
        @JvmStatic private val HANDLERS = HandlerList()

        @JvmStatic fun getHandlerList(): HandlerList {
            return HANDLERS
        }
    }

    override fun getHandlers(): HandlerList {
        return HANDLERS
    }
}