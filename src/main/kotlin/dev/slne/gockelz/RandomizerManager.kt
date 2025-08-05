package dev.slne.gockelz

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.github.shynixn.mccoroutine.folia.ticks
import dev.slne.surf.surfapi.bukkit.api.extensions.server
import dev.slne.surf.surfapi.core.api.messages.Colors
import dev.slne.surf.surfapi.core.api.messages.CommonComponents
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.messages.adventure.playSound
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.surfapi.core.api.messages.adventure.showTitle
import dev.slne.surf.surfapi.core.api.util.mutableObjectSetOf
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.util.*
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object RandomizerManager {

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
                        append(
                            CommonComponents.formatTime(
                                seconds.seconds,
                                showSeconds = true,
                                shortForms = false,
                                separator = buildText {
                                    variableValue(":")
                                },
                                timeColor = Colors.VARIABLE_VALUE
                            ),
                        )
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

    private fun notifyStart() = plugin.launch {
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
            }
        }
    }

    private fun notifyEnd() = plugin.launch {
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
    }

    val players = mutableObjectSetOf<UUID>()

    var randomizerTaskSeconds = 0
    var randomizerTask: Job? = null

    fun start(
        players: Collection<Player>,
        timeout: Int,
        timeBetweenRandoms: Int,
        delayToFirstRandom: Int?
    ) {
        this.players.clear()
        this.players.addAll(players.map { it.uniqueId })

        if (randomizerTask != null) error("Randomizer task is already running!")

        randomizerTaskSeconds = timeout * 20

        randomizerTask = plugin.launch {
            if (delayToFirstRandom != null) {
                delay(delayToFirstRandom.ticks)
            }

            notifyStart()

            while (isActive && randomizerTaskSeconds > 0) {
                notifyIfApplicable(randomizerTaskSeconds)

                randomizerTaskSeconds--
                delay(timeBetweenRandoms.ticks)
            }

            notifyEnd()
        }
    }

    fun stop() {
        if (randomizerTask == null) error("Randomizer task is not running!")

        randomizerTask?.cancel()
        randomizerTask = null

        notifyEnd()

        players.clear()
    }

    fun isRunning() = randomizerTask != null && randomizerTask?.isActive == true

}