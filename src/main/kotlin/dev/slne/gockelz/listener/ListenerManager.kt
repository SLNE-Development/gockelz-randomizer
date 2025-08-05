package dev.slne.gockelz.listener

import dev.slne.surf.surfapi.bukkit.api.event.register

object ListenerManager {

    fun register() {
        BlockListener.register()
        OnlineListener.register()
    }

}