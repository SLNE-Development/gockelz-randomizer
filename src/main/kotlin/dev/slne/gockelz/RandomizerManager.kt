package dev.slne.gockelz

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.gockelz.distance.DistanceService
import dev.slne.surf.surfapi.bukkit.api.extensions.server
import dev.slne.surf.surfapi.core.api.messages.adventure.playSound
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.surfapi.core.api.messages.adventure.showTitle
import dev.slne.surf.surfapi.core.api.util.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.time.ZonedDateTime
import java.util.*
import kotlin.random.asKotlinRandom
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object RandomizerManager {

    private val invalidItems = objectSetOf(

        // Spawn Eggs
        Material.ENDER_DRAGON_SPAWN_EGG,
        Material.WITHER_SPAWN_EGG,
        Material.WARDEN_SPAWN_EGG,
        Material.BLAZE_SPAWN_EGG,
        Material.GHAST_SPAWN_EGG,
        Material.HAPPY_GHAST_SPAWN_EGG,
        Material.SHULKER_SPAWN_EGG,

        // Creative only items
        Material.COMMAND_BLOCK,
        Material.COMMAND_BLOCK_MINECART,
        Material.CHAIN_COMMAND_BLOCK,
        Material.REPEATING_COMMAND_BLOCK,
        Material.BARRIER,
        Material.STRUCTURE_VOID,
        Material.STRUCTURE_BLOCK,
        Material.JIGSAW,
        Material.END_PORTAL_FRAME,
        Material.DRAGON_EGG,
        Material.SPAWNER,
        Material.DEBUG_STICK,
        Material.BEDROCK,
        Material.LIGHT,
        Material.TEST_BLOCK,
        Material.TEST_INSTANCE_BLOCK,
        Material.REINFORCED_DEEPSLATE,
        Material.VAULT,
        Material.TRIAL_SPAWNER,

        // Other
        Material.ELYTRA
    )

    private fun isInvalid(material: Material): Boolean =
        material == Material.AIR || !material.isItem || material in invalidItems

    private val items = Material.entries
        .filterNot(::isInvalid)
        .map { ItemStack(it) }
        .toObjectSet()

    private val notifyAt = mutableObjectSetOf(
        90.minutes,
        60.minutes,
        30.minutes,
        20.minutes,
        10.minutes,
        5.minutes,
        3.minutes,
        2.minutes,
        1.minutes,
        30.seconds,
        15.seconds,
        10.seconds,
        5.seconds,
        4.seconds,
        3.seconds,
        2.seconds,
        1.seconds
    )

    private fun notifyIfApplicable(seconds: Int) = plugin.launch {
        players.mapNotNull { server.getPlayer(it) }.forEach { player ->
            notifyAt.firstOrNull { it.inWholeSeconds == seconds.toLong() }?.let {
                withContext(plugin.entityDispatcher(player)) {
                    player.sendText {
                        appendPrefix()

                        info("Der Randomizer endet in ")
                        variableValue(seconds.seconds.toString())
                        info(".")
                    }

                    player.playSound(true) {
                        type(Sound.BLOCK_NOTE_BLOCK_PLING)
                        volume(.5f)
                    }
                }
            }
        }
    }

    private fun notifyAndPreparePlayer() = plugin.launch {
        players.mapNotNull { server.getPlayer(it) }.forEach { player ->
            withContext(plugin.entityDispatcher(player)) {
                player.showTitle {
                    title {
                        primary("Randomizer gestartet")
                    }
                    subtitle {
                        variableValue("Viel Erfolg!")
                    }
                    times {
                        fadeIn(500.milliseconds)
                        stay(3.seconds)
                        fadeOut(500.milliseconds)
                    }
                }

                player.playSound(true) {
                    type(Sound.ENTITY_ENDER_DRAGON_GROWL)
                    volume(.5f)
                    pitch(.5f)
                }

                MapManager.getPlayerSpawnLocation(player)?.let { player.teleportAsync(it.add(0.5, 1.0, 0.5).setRotation(-90.0f, 0.0f)) }
                player.health = 20.0
                player.foodLevel = 20
            }
        }
    }

    private fun notifyEnd(after: () -> Unit) = plugin.launch {
        players.mapNotNull { server.getPlayer(it) }.forEach { player ->
            withContext(plugin.entityDispatcher(player)) {
                player.showTitle {
                    title {
                        primary("Randomizer beendet")
                    }
                    subtitle {
                        variableValue("Danke fürs Mitmachen!")
                    }
                    times {
                        fadeIn(500.milliseconds)
                        stay(3.seconds)
                        fadeOut(500.milliseconds)
                    }
                }

                player.playSound(true) {
                    type(Sound.ENTITY_ENDER_DRAGON_GROWL)
                    volume(.5f)
                    pitch(.5f)
                }
            }
        }

        after()
    }

    private val _players = mutableObjectSetOf<UUID>()
    val players = _players.freeze()

    private val lockedPlayers = mutableObject2ObjectMapOf<UUID, ZonedDateTime>()

    fun addLockedPlayer(player: Player) = lockedPlayers.put(player.uniqueId, ZonedDateTime.now())

    var randomizerTaskSeconds = 0
    var secondsTask: Job? = null
    var randomizerTask: Job? = null
    var lockTask: Job? = null

    suspend fun start(
        players: Collection<Player>,
        timeout: Int,
        timeBetweenRandoms: Int,
        lockPlayersFor: Int,
        delayToFirstRandom: Int?,
        spawnAtBedrock: Boolean = false
    ) {
        if (randomizerTask != null) error("Randomizer task is already running!")

        DistanceService.resetForNewGame()

        randomizerTaskSeconds = timeout

        this.lockedPlayers.clear()
        this._players.clear()
        this._players.addAll(players.map { it.uniqueId })

        notifyAndPreparePlayer()

        if (delayToFirstRandom != null) {
            delay(delayToFirstRandom.seconds)
        }

        lockTask = plugin.launch {
            while (isActive && randomizerTaskSeconds > 0) {
                val now = ZonedDateTime.now()

                players.forEach { player ->
                    if (!player.isOnline) {
                        _players.remove(player.uniqueId)
                        return@forEach
                    }

                    val lockedAt = lockedPlayers[player.uniqueId]

                    if (lockedAt != null) {
                        if (now.isAfter(lockedAt.plusSeconds(lockPlayersFor.toLong()))) {
                            lockedPlayers.remove(player.uniqueId)
                        } else {
                            val remainingSeconds = lockedAt.plusSeconds(lockPlayersFor.toLong())
                                .toEpochSecond() - now.toEpochSecond()

                            player.sendText {
                                appendPrefix()

                                info("Du bist noch für ")
                                variableValue(remainingSeconds.seconds.toString())
                                info(" gesperrt, weil du gestorben bist und erhältst somit kein Item.")
                            }

                            return@forEach
                        }
                    }
                }

                delay(1.seconds)
            }
        }

        secondsTask = plugin.launch {
            while (isActive && randomizerTaskSeconds > 0) {
                notifyIfApplicable(randomizerTaskSeconds)

                players.forEach { player ->
                    if (!player.isOnline) {
                        _players.remove(player.uniqueId)
                        return@forEach
                    }
                }

                randomizerTaskSeconds--

                delay(1.seconds)
            }

            stop()
        }

        randomizerTask = plugin.launch {
            while (isActive && randomizerTaskSeconds > 0) {
                players.forEach { player ->
                    if (!player.isOnline) {
                        _players.remove(player.uniqueId)
                        return@forEach
                    }

                    if (lockedPlayers[player.uniqueId] != null) {
                        return@forEach
                    }

                    withContext(plugin.entityDispatcher(player)) {
                        val item = randomValidItem()

                        if (spawnAtBedrock) {
                            val spawnLocation = MapManager.getPlayerSpawnLocation(player)

                            if (spawnLocation == null) {
                                player.inventory.addItem(item)
                                return@withContext
                            }

                            spawnLocation.world.dropItem(
                                spawnLocation.clone().add(0.5, 1.0, 0.5),
                                item
                            ) { entity ->
                                entity.velocity = Vector(0, -1, 0)
                                entity.owner = player.uniqueId
                            }
                        } else {
                            player.inventory.addItem(item)
                        }
                    }
                }

                delay(timeBetweenRandoms.seconds)
            }
        }
    }

    fun stop() {
        if (randomizerTask == null || lockTask == null || secondsTask == null) {
            error("Randomizer task is not running!")
        }

        secondsTask?.cancel()
        secondsTask = null

        lockTask?.cancel()
        lockTask = null

        randomizerTask?.cancel()
        randomizerTask = null

        notifyEnd {
            _players.clear()
            lockedPlayers.clear()
        }
    }

    fun isRunning() = randomizerTask != null && randomizerTask?.isActive == true

    private fun randomValidItem(): ItemStack {
        var lastInvalid: Material? = null

        repeat(10) {
            val candidate = items.random(random.asKotlinRandom())
            if (!isInvalid(candidate.type)) {
                return candidate
            } else {
                lastInvalid = candidate.type
            }
        }

        lastInvalid?.let {
            plugin.logger.warning("[Randomizer] Found invalid item: $it")
        }
        return ItemStack(Material.STONE)
    }

}