package dev.slne.gockelz

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.gockelz.commands.randomizerCommand
import dev.slne.gockelz.listener.ListenerManager
import dev.slne.gockelz.distance.DistanceService
import org.bukkit.plugin.java.JavaPlugin

val plugin get() = JavaPlugin.getPlugin(RandomizerPlugin::class.java)

class RandomizerPlugin : SuspendingJavaPlugin() {

    override suspend fun onEnableAsync() {
        RandomizerManager

        ListenerManager.register()
        randomizerCommand()

        DistanceService.start()
    }

    override suspend fun onDisableAsync() {
        DistanceService.stop()
    }
}
