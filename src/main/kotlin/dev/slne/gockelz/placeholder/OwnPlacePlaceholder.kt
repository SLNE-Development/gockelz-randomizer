package dev.slne.gockelz.placeholder

import dev.slne.gockelz.distance.DistanceService
import dev.slne.surf.surfapi.bukkit.api.hook.papi.expansion.PapiPlaceholder
import dev.slne.surf.surfapi.core.api.messages.Colors
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import kotlin.math.max

/**
 * %gockelz_ownplace%
 * %gockelz_ownplace_plus_{count}%
 * %gockelz_ownplace_minus_{count}%
 */
object OwnPlacePlaceholder : PapiPlaceholder("ownplace") {
    override fun parse(player: OfflinePlayer, args: List<String>): String? {
        val op = args.getOrNull(0)?.lowercase()?.trim()
        val n = args.getOrNull(1)?.toIntOrNull() ?: 1

        val myPlace0 = DistanceService.placeOfPlayer(player.uniqueId)
        if (myPlace0 == Int.MAX_VALUE) return "Du bist nicht im Ranking."

        val targetPlace0 = when (op) {
            "plus", "+", "p", "ahead"  -> myPlace0 - n
            "minus", "-", "m", "behind"-> myPlace0 + n
            null, "", "self"           -> myPlace0
            else -> return null
        }.let { max(0, it) }

        val targetPlaceOneBased = targetPlace0 + 1L

        val targetUuid = DistanceService.playerAtPlace(targetPlaceOneBased)
            ?: return "Letzter…?"

        val dist = DistanceService.distanceBlocks(targetUuid)

        return LegacyComponentSerializer.legacySection().serialize(buildText {
            text("${targetPlaceOneBased}. ", Colors.WHITE)
            text(Bukkit.getOfflinePlayer(targetUuid).name ?: "null")
            spacer(": ")
            variableValue(dist)
        })
    }
}
