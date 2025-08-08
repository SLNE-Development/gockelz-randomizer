package dev.slne.gockelz

import dev.slne.surf.surfapi.bukkit.api.extensions.server
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

object MapManager {

    const val RANDOMIZER_Y: Int = 100
    private const val DISTANCE_BETWEEN_SPAWNS = 10
    private val lastSpawnZKey = NamespacedKey("gockelz", "last_spawn_z")

    private const val WORLD_NAME = "gockelz"

    private val randomizerWorld get() = server.getWorld(WORLD_NAME)

    private val playerXKey = NamespacedKey("gockelz", "player_x")
    private val playerZKey = NamespacedKey("gockelz", "player_z")

    fun getPlayerSpawnLocation(player: Player): Location? {
        val pdc = player.persistentDataContainer
        val x = pdc.get(playerXKey, PersistentDataType.INTEGER)
        val z = pdc.get(playerZKey, PersistentDataType.INTEGER)

        if (x == null || z == null) {
            return null
        }

        return Location(randomizerWorld, x.toDouble(), RANDOMIZER_Y.toDouble(), z.toDouble())
    }

    private fun setSpawnLocation(player: Player, location: Location) {
        val pdc = player.persistentDataContainer

        pdc.set(playerXKey, PersistentDataType.INTEGER, location.blockX)
        pdc.set(playerZKey, PersistentDataType.INTEGER, location.blockZ)
    }

    fun prepareSpawnpoint(player: Player): Pair<Boolean, Location> {
        val playerSpawn = getPlayerSpawnLocation(player)

        if (playerSpawn != null) {
            return true to playerSpawn
        }

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

        val respawn = block.location.clone().add(0.5, 1.0, 0.5)
        player.respawnLocation = respawn
        setSpawnLocation(player, respawn)

        world.persistentDataContainer.set(lastSpawnZKey, PersistentDataType.INTEGER, spawnZ)

        return false to respawn
    }
}