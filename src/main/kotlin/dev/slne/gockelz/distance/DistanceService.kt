package dev.slne.gockelz.distance

import com.github.shynixn.mccoroutine.folia.launch
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

/**
 * Caches the max reached distance per player along +X relative to spawn
 *   current = max(0, x - spawnX)
 *   cachedMax = max(cachedMax, current)
 */
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
                try { recomputeAll() } catch (t: Throwable) {
                    plugin.logger.warning("[DistanceService] Recompute-Fehler: ${t.message}")
                }
                delay(UPDATE_PERIOD_MS)
            }
        }
    }

    fun stop() {
        job?.cancel(); job = null
        lock.write {
            maxByPlayer.clear()
            sortedByMax = emptyList()
            rankByPlayer.clear()
        }
    }

    private fun recomputeAll() {
        val online = server.onlinePlayers.toList()
        if (online.isEmpty()) return

        // Update cached max if current progress is greater
        for (p in online) {
            val current = forwardProgressX(p)
            maxByPlayer.compute(p.uniqueId) { _, prev -> kotlin.math.max(prev ?: 0L, current) }
        }

        // Ranking by cachedMax
        val sorted = maxByPlayer.entries
            .sortedByDescending { it.value }
            .map { it.key to it.value }

        lock.write {
            sortedByMax = sorted
            rankByPlayer.clear()
            var i = 0
            for ((uuid, _) in sortedByMax) rankByPlayer[uuid] = i++
        }
    }

    // forward progress on track = max(0, x - spawnX)
    private fun forwardProgressX(p: Player): Long {
        val spawn = spawnLocationOf(p) ?: p.world.spawnLocation
        val dxPlus = p.location.x - spawn.x
        return dxPlus.toLong().coerceAtLeast(0L)
    }

    private fun spawnLocationOf(p: Player): Location? =
        p.respawnLocation ?: p.bedSpawnLocation

    fun placeOfPlayer(uuid: UUID): Int = lock.read { rankByPlayer[uuid] ?: Int.MAX_VALUE }

    fun playerAtPlace(placeOneBased: Long): UUID? = lock.read {
        val idx = (placeOneBased - 1).toInt()
        if (idx in sortedByMax.indices) sortedByMax[idx].first else null
    }

    fun distanceBlocks(uuid: UUID): Long = lock.read { maxByPlayer[uuid] } ?: 0L

    fun distanceBlocksAtPlace(placeOneBased: Long): Long? = lock.read {
        val idx = (placeOneBased - 1).toInt()
        if (idx in sortedByMax.indices) sortedByMax[idx].second else null
    }
}
