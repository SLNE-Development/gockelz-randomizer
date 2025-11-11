package dev.slne.gockelz.placeholder

import dev.slne.gockelz.distance.DistanceService
import dev.slne.surf.surfapi.bukkit.api.extensions.server
import dev.slne.surf.surfapi.bukkit.api.hook.papi.expansion.PapiPlaceholder
import dev.slne.surf.surfapi.core.api.messages.Colors
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.OfflinePlayer

/**
 * %gockelz_place_{place}%
 */
object PlacePlaceholder : PapiPlaceholder("place") {
    override fun parse(player: OfflinePlayer, args: List<String>): String? {
        require(args.size == 1) { "Invalid number of arguments. A place must be provided." }
        val placeOneBased = args[0].toLongOrNull() ?: return null

        val targetUuid = DistanceService.playerAtPlace(placeOneBased) ?: return "$placeOneBased. ???"
        val targetPlayer = server.getOfflinePlayer(targetUuid)
        val dist = DistanceService.distanceBlocks(targetUuid)

        return LegacyComponentSerializer.legacySection().serialize(buildText {
            text("$placeOneBased. ", Colors.WHITE)
            text(targetPlayer.name ?: "null")
            spacer(": ")
            variableValue(dist)
        })
    }
}
