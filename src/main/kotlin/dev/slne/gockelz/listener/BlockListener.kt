package dev.slne.gockelz.listener

import dev.slne.gockelz.MapManager
import dev.slne.gockelz.RandomizerManager
import dev.slne.gockelz.utils.PermissionRegistry
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
        val player = event.player

        checkGameIsRunning(player, event) {
            checkAndCancelAboveSpawnpoint(event) {
                checkInOwnLine(event, player, event) {
                    checkOnCorrectY(event) {
                        checkPlacingInPositiveX(event) {

                        }
                    }
                }
            }
        }
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val player = event.player

        if (!player.hasPermission(PermissionRegistry.COMMAND_BASE)) {
            player.sendText {
                appendPrefix()

                error("Du hast keine Berechtigung, Blöcke abzubauen!")
            }

            event.isCancelled = true
            return
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

    private fun checkOnCorrectY(event: BlockPlaceEvent, next: () -> Unit) {
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
        val spawnPoint = player.respawnLocation ?: return

        val blockX = event.block.x
        val spawnX = spawnPoint.blockX

        if (blockX != spawnX) {
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

            return
        }

        next()
    }

}