package dev.slne.gockelz.commands.subcommands

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.*
import dev.slne.gockelz.RandomizerManager
import dev.slne.gockelz.plugin
import dev.slne.gockelz.utils.PermissionRegistry
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.messages.adventure.clickSuggestsCommand
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.entity.Player

fun CommandAPICommand.randomizerStartCommand() = subcommand("start") {
    withPermission(PermissionRegistry.COMMAND_START)

    entitySelectorArgumentManyPlayers("players")
    integerArgument("timeout", min = 1)
    integerArgument("timeBetweenRandoms", min = 1)
    integerArgument("delayToFirstRandom", min = 0, optional = true)
    booleanArgument("spawnAtBedrock", optional = true)

    playerExecutor { player, arguments ->
        val players: List<Player> by arguments
        val timeout: Int by arguments
        val timeBetweenRandoms: Int by arguments
        val delayToFirstRandom: Int? by arguments
        val spawnAtBedrock: Boolean by arguments

        plugin.launch {
            if (RandomizerManager.randomizerTask != null) {
                player.sendText {
                    appendPrefix()

                    error("Der Randomizer ist bereits gestartet! Bitte stoppe ihn zuerst mit ")
                    append {
                        variableValue("/randomizer stop", TextDecoration.UNDERLINED)
                        clickSuggestsCommand("/randomizer stop")
                        hoverEvent(HoverEvent.showText(buildText {
                            info("Klicke hier, um den Befehl auszuführen.")
                        }))
                    }
                    error(".")
                }

                return@launch
            }

            RandomizerManager.start(
                players,
                timeout,
                timeBetweenRandoms,
                delayToFirstRandom,
                spawnAtBedrock
            )

            player.sendText {
                appendPrefix()

                success("Der Randomizer wurde erfolgreich gestartet!")
            }
        }
    }
}