package dev.slne.gockelz.listener

import dev.slne.gockelz.MapManager
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockEvent
import org.bukkit.event.block.BlockPlaceEvent

object BlockListener : Listener {

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        checkAndCancelAboveSpawnpoint(event)
        checkInOwnLine(event, event.player, event)
        checkOnCorrectY(event)
        checkPlacingInPositiveX(event)
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        checkInOwnLine(event, event.player, event)
    }

    private fun checkPlacingInPositiveX(event: BlockPlaceEvent) {
        val player = event.player
        val blockX = event.blockPlaced.x

        if (blockX <= 0) {
            event.isCancelled = true

            player.sendText {
                appendPrefix()

                error("Du kannst Blöcke nur in der positiven X-Richtung platzieren!")
            }
        }
    }

    private fun checkOnCorrectY(event: BlockPlaceEvent) {
        val player = event.player
        val blockY = event.blockPlaced.y

        if (blockY != MapManager.RANDOMIZER_Y) {
            event.isCancelled = true

            player.sendText {
                appendPrefix()

                error("Du kannst Blöcke nur auf Höhe ")
                variableValue(MapManager.RANDOMIZER_Y)
                error(" platzieren!")
            }
        }
    }

    private fun checkInOwnLine(event: BlockEvent, player: Player, cancellable: Cancellable) {
        val spawnPoint = player.respawnLocation ?: return

        val blockX = event.block.x
        val spawnX = spawnPoint.blockX

        if (blockX != spawnX) {
            cancellable.isCancelled = true

            player.sendText {
                appendPrefix()

                error("Du kannst keine Blöcke außerhalb deiner eigenen Linie platzieren oder abbauen!")
            }
        }
    }

    private fun checkAndCancelAboveSpawnpoint(event: BlockPlaceEvent) {
        val player = event.player
        val spawnPoint = player.respawnLocation ?: return

        val blockX = event.blockPlaced.x
        val blockZ = event.blockPlaced.z

        val spawnX = spawnPoint.blockX
        val spawnZ = spawnPoint.blockZ

        if (blockX == spawnX && blockZ == spawnZ) {
            event.isCancelled = true

            player.sendText {
                appendPrefix()

                error("Du kannst keine Blöcke direkt über deinem Spawnpunkt platzieren!")
            }
        }
    }

}