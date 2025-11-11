package dev.slne.gockelz.placeholder

import dev.slne.gockelz.RandomizerManager
import dev.slne.gockelz.distance.DistanceService
import dev.slne.surf.surfapi.bukkit.api.hook.papi.expansion.PapiPlaceholder
import org.bukkit.OfflinePlayer

/**
 * %gockelz_scoreboard-selector%
 */
object ScoreboardSelectorPlaceholder : PapiPlaceholder("scoreboard-selector") {
    override fun parse(player: OfflinePlayer, args: List<String>): String? {

        if (!RandomizerManager.isRunning()) {
            return "not-running"
        }

        val place0 = DistanceService.placeOfPlayer(player.uniqueId)
        return if (place0 < 10) "scoreboard01" else "scoreboard02"
    }
}
