package dev.slne.gockelz

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.gockelz.commands.randomizerCommand
import dev.slne.gockelz.listener.ListenerManager
import org.bukkit.plugin.java.JavaPlugin

val plugin get() = JavaPlugin.getPlugin(RandomizerPlugin::class.java)

class RandomizerPlugin : SuspendingJavaPlugin() {

    override suspend fun onEnableAsync() {
        // Initialize the material list, like wth?
        RandomizerManager

        ListenerManager.register()
        randomizerCommand()
    }
}