plugins {
    id("blahaj-convention")
    id("net.neoforged.moddev") version "2.0.148"
}

val modId = property("modId") as String

neoForge {
    version = property("neoforgeVersion") as String

    runs {
        register("client") {
            client()
            gameDirectory = file("runs/client")
        }
        register("server") {
            server()
            gameDirectory = file("runs/server")
            programArgument("--nogui")
        }
    }

    mods {
        register(modId) {
            sourceSet(sourceSets.main.get())
        }
    }
}

dependencies {
    implementation("com.geckolib:geckolib-neoforge-${property("minecraftVersion")}:${property("geckolibVersion")}")
}

val props = mapOf(
    "version" to project.version,
    "mod_id" to modId,
    "mod_name" to property("modName"),
    "mod_authors" to property("modAuthors"),
    "mod_description" to property("modDescription"),
    "minecraft_version" to property("minecraftVersion"),
    "geckolib_version" to property("geckolibVersion"),
)

tasks.processResources {
    inputs.properties(props)
    filesMatching("META-INF/neoforge.mods.toml") { expand(props) }
}
