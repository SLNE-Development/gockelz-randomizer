package dev.slne.gockelz.distance

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.gockelz.RandomizerManager
import dev.slne.gockelz.plugin
import dev.slne.surf.surfapi.bukkit.api.extensions.server
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

object DistanceService {
    private const val UPDATE_PERIOD_MS = 1000L

    private val lock = ReentrantReadWriteLock()
    private val maxByPlayer = ConcurrentHashMap<UUID, Long>()
    private var sortedByMax: List<Pair<UUID, Long>> = emptyList()
    private val rankByPlayer = HashMap<UUID, Int>()
    private var job: Job? = null

    fun start() {
        if (job != null) return
        job = plugin.launch {
            while (true) {
                try {
                    recomputeAll()
                } catch (t: Throwable) {
                    plugin.logger.warning("[DistanceService] Recompute error: ${t.message}")
                }
                delay(UPDATE_PERIOD_MS)
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        resetForNewGame()
    }

    fun resetForNewGame() {
        lock.write {
            maxByPlayer.clear()
            sortedByMax = emptyList()
            rankByPlayer.clear()
        }
    }

    /**
     * Runs in a coroutine thread, but all world access is done
     * on the corresponding entity dispatcher for each player.
     */
    private suspend fun recomputeAll() {
        // Only collect progress while the game is running
        if (!RandomizerManager.isRunning()) return

        val online = server.onlinePlayers.toList()
        if (online.isEmpty()) return

        // First compute progress per player on their entity thread
        val localProgress = HashMap<UUID, Long>(online.size)

        for (player in online) {
            val progress = withContext(plugin.entityDispatcher(player)) {
                forwardProgressX(player)
            }
            localProgress[player.uniqueId] = progress
        }

        // Then apply results to shared state under a write lock
        lock.write {
            for ((uuid, progress) in localProgress) {
                maxByPlayer.compute(uuid) { _, prev ->
                    kotlin.math.max(prev ?: 0L, progress)
                }
            }

            val sorted = maxByPlayer.entries
                .sortedByDescending { it.value }
                .map { it.key to it.value }

            sortedByMax = sorted
            rankByPlayer.clear()
            var i = 0
            for ((uuid, _) in sortedByMax) {
                rankByPlayer[uuid] = i++
            }
        }
    }

    // forward progress on track = max(0, x - spawnX)
    private fun forwardProgressX(p: Player): Long {
        val loc = p.location
        val world = loc.world

        // Check if there is any block in this column (x, z).
        val highest = world.getHighestBlockAt(loc.blockX, loc.blockZ)
        if (highest.type.isAir) {
            return 0L
        }

        val spawn = spawnLocationOf(p) ?: world.spawnLocation
        val dxPlus = loc.x - spawn.x
        return dxPlus.toLong().coerceAtLeast(0L)
    }

    private fun spawnLocationOf(p: Player): Location? =
        p.respawnLocation ?: p.bedSpawnLocation

    fun placeOfPlayer(uuid: UUID): Int =
        lock.read { rankByPlayer[uuid] ?: Int.MAX_VALUE }

    fun playerAtPlace(placeOneBased: Long): UUID? = lock.read {
        val idx = (placeOneBased - 1).toInt()
        if (idx in sortedByMax.indices) sortedByMax[idx].first else null
    }

    fun distanceBlocks(uuid: UUID): Long =
        lock.read { maxByPlayer[uuid] } ?: 0L

    fun distanceBlocksAtPlace(placeOneBased: Long): Long? = lock.read {
        val idx = (placeOneBased - 1).toInt()
        if (idx in sortedByMax.indices) sortedByMax[idx].second else null
    }

    fun hasAnyData(): Boolean =
        lock.read { sortedByMax.isNotEmpty() }
}
