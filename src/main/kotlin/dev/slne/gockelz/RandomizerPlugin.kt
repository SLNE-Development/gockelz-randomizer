package dev.slne.gockelz

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import org.bukkit.plugin.java.JavaPlugin

val plugin get() = JavaPlugin.getPlugin(RandomizerPlugin::class.java)

class RandomizerPlugin : SuspendingJavaPlugin() {


}