package dev.slne.gockelz.listener

import dev.slne.gockelz.MapManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

object OnlineListener : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        MapManager.prepareSpawnpoint(event.player)
    }

}