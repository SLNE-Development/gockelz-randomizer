package dev.slne.gockelz.commands

import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.slne.gockelz.commands.subcommands.randomizerStartCommand
import dev.slne.gockelz.commands.subcommands.randomizerStopCommand
import dev.slne.gockelz.utils.PermissionRegistry

fun randomizerCommand() = commandAPICommand("randomizer") {
    withPermission(PermissionRegistry.COMMAND_BASE)

    randomizerStartCommand()
    randomizerStopCommand()
}