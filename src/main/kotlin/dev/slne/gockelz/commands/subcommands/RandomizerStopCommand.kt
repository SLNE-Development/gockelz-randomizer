package dev.slne.gockelz.commands.subcommands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.gockelz.RandomizerManager
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText

fun CommandAPICommand.randomizerStopCommand() = subcommand("stop") {
    playerExecutor { player, args ->
        if (RandomizerManager.randomizerTask == null) {
            player.sendText {
                appendPrefix()

                error("Der Randomizer ist nicht gestartet!")
            }

            return@playerExecutor
        }

        RandomizerManager.stop()

        player.sendText {
            appendPrefix()

            success("Der Randomizer wurde erfolgreich gestoppt!")
        }
    }
}