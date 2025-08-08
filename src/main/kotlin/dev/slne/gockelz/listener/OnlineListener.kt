package dev.slne.gockelz.listener

import dev.slne.gockelz.MapManager
import dev.slne.gockelz.RandomizerManager
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerRespawnEvent

object OnlineListener : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val (hadSpawn, location) = MapManager.prepareSpawnpoint(player)

        if (!hadSpawn) {
            val spawnLocation = location.clone().add(0.5, 2.0, 0.5)
            spawnLocation.yaw = 90f

            player.teleportAsync(spawnLocation)
        }
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        RandomizerManager.addLockedPlayer(event.player)
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

        val spawnLocation = respawn.clone().add(0.5, 2.0, 0.5)
        spawnLocation.yaw = 90f

        player.teleportAsync(spawnLocation)
    }

}