package dev.slne.gockelz

import dev.slne.surf.surfapi.bukkit.api.extensions.server
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

object MapManager {

    const val RANDOMIZER_Y: Int = 100
    private const val DISTANCE_BETWEEN_SPAWNS = 5
    private val lastSpawnZKey = NamespacedKey("gockelz", "last_spawn_z")

    private const val WORLD_NAME = "gockelz_void_world"

    private val randomizerWorld get() = server.getWorld(WORLD_NAME)

    fun prepareSpawnpoint(player: Player) {
        val world = randomizerWorld ?: error("RandomizerWorld is null")
        val lastSpawnZ =
            world.persistentDataContainer.get(lastSpawnZKey, PersistentDataType.INTEGER) ?: -1

        val spawnZ = if (lastSpawnZ < 0) {
            0
        } else {
            lastSpawnZ + DISTANCE_BETWEEN_SPAWNS
        }

        val block = world.getBlockAt(0, RANDOMIZER_Y, spawnZ)
        block.type = Material.BEDROCK

        player.respawnLocation = block.location.clone().add(0.0, 1.0, 0.0)

        world.persistentDataContainer.set(lastSpawnZKey, PersistentDataType.INTEGER, spawnZ)
    }
}