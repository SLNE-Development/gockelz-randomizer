package dev.slne.gockelz.placeholder

import dev.slne.surf.surfapi.bukkit.api.hook.papi.expansion.PapiExpansion

object RandomizerPlaceholderExpansion : PapiExpansion(
    "gockelz",
    listOf(
        ScoreboardSelectorPlaceholder,
        PlacePlaceholder,
        OwnPlacePlaceholder,
    )
)
