package dev.slne.gockelz.utils

import dev.slne.surf.surfapi.bukkit.api.permission.PermissionRegistry

object PermissionRegistry : PermissionRegistry() {

    private const val PREFIX = "gockelz"
    private const val COMMAND_PREFIX = "$PREFIX.command"

    val COMMAND_BASE = create("$COMMAND_PREFIX.base")
    val COMMAND_START = create("$COMMAND_PREFIX.start")
    val COMMAND_STOP = create("$COMMAND_PREFIX.stop")

}