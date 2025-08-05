package dev.slne.gockelz.listener

import dev.slne.gockelz.MapManager
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerRespawnEvent

object OnlineListener : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val (hadSpawn, location) = MapManager.prepareSpawnpoint(player)

        if (!hadSpawn) {
            player.teleportAsync(location)
        }
    }

    @EventHandler
    fun onPlayerRespawn(event: PlayerRespawnEvent) {
        val player = event.player
        val respawn = MapManager.getPlayerSpawnLocation(player)

        if (respawn == null) {
            player.sendText {
                appendPrefix()

                error("Es konnte kein Spawnpunkt für dich gefunden werden. Bitte kontaktiere einen Administrator.")
            }

            return
        }

        player.teleportAsync(respawn)
    }

}