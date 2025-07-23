import dev.slne.surf.surfapi.gradle.util.withSurfApiBukkit

plugins {
    id("dev.slne.surf.surfapi.gradle.paper-plugin")
}

group = "dev.slne.gockelz"

surfPaperPluginApi {
    mainClass("dev.slne.gockelz.RandomizerPlugin")
    generateLibraryLoader(false)
    authors.add("Ammo")

    runServer {
        withSurfApiBukkit()
    }
}