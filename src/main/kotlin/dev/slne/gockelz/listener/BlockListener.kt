package dev.slne.gockelz.listener

import dev.slne.gockelz.MapManager
import dev.slne.gockelz.RandomizerManager
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.surfapi.core.api.util.mutableObjectSetOf
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockEvent
import org.bukkit.event.block.BlockPlaceEvent

object BlockListener : Listener {

    private const val ALLOWED_RADIUS = 5

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        val player = event.player

        checkGameIsRunning(player, event) {
            checkAndCancelAboveSpawnpoint(event) {
                checkInOwnLine(event, player, event) {
                    checkPlacingInPositiveX(event) {

                    }
                }
            }
        }
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val player = event.player

        checkGameIsRunning(player, event) {
            checkInOwnLine(event, player, event) {

            }
        }
    }

    private fun checkGameIsRunning(player: Player, cancellable: Cancellable, next: () -> Unit) {
        if (!RandomizerManager.isRunning()) {
            player.sendText {
                appendPrefix()

                error("Das Spiel läuft nicht! Du kannst keine Blöcke abbauen oder platzieren.")
            }

            cancellable.isCancelled = true
            return
        }

        next()
    }

    private fun checkPlacingInPositiveX(event: BlockPlaceEvent, next: () -> Unit) {
        val player = event.player
        val blockX = event.blockPlaced.x

        if (blockX <= 0) {
            event.isCancelled = true

            player.sendText {
                appendPrefix()

                error("Du kannst Blöcke nur in der positiven X-Richtung platzieren!")
            }

            return
        }

        next()
    }

    private fun checkInOwnLine(
        event: BlockEvent,
        player: Player,
        cancellable: Cancellable,
        next: () -> Unit
    ) {
        val spawnPoint = MapManager.getPlayerSpawnLocation(player) ?: run {
            player.sendText {
                appendPrefix()

                error("Es konnte kein Spawnpunkt für dich gefunden werden. Bitte kontaktiere einen Administrator.")
            }

            cancellable.isCancelled = true
            return
        }

        val blockZ = event.block.z
        val spawnZ = spawnPoint.blockZ
        val allowedRadius = ALLOWED_RADIUS
        val allowed = mutableObjectSetOf<Int>()
        for (i in -allowedRadius..allowedRadius) {
            allowed.add(spawnZ + i)
        }

        if (blockZ !in allowed) {
            cancellable.isCancelled = true

            player.sendText {
                appendPrefix()

                error("Du kannst keine Blöcke außerhalb deiner eigenen Linie platzieren oder abbauen!")
            }

            return
        }

        next()
    }

    private fun checkAndCancelAboveSpawnpoint(event: BlockPlaceEvent, next: () -> Unit) {
        val player = event.player
        val spawnPoint = MapManager.getPlayerSpawnLocation(player) ?: run {
            player.sendText {
                appendPrefix()

                error("Es konnte kein Spawnpunkt für dich gefunden werden. Bitte kontaktiere einen Administrator.")
            }

            event.isCancelled = true
            return
        }

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

            return
        }

        next()
    }

}