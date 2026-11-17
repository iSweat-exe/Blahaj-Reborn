plugins {
    id("blahaj-convention")
    id("net.minecraftforge.gradle") version "7.0.40"
}

val modId = property("modId") as String

minecraft {
    runs {
        configureEach {
            workingDir.convention(layout.projectDirectory.dir("runs/$name"))
            args("--mixin.config=blahaj.mixins.json")
        }
        register("client") {}
        register("server") {}
    }
}

repositories {
    maven(minecraft.mavenizer)
    maven(fg.forgeMaven)
    maven(fg.minecraftLibsMaven)
    mavenCentral()
}

dependencies {
    implementation(minecraft.dependency("net.minecraftforge:forge:${property("forgeVersion")}"))
    implementation("com.geckolib:geckolib-forge-${property("minecraftVersion")}:${property("geckolibVersion")}")
}

// Forge reads the Mixin config from the jar manifest.
tasks.jar {
    manifest { attributes("MixinConfigs" to "blahaj.mixins.json") }
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
    filesMatching("META-INF/mods.toml") { expand(props) }
}

// In dev, Forge needs classes and resources in the same folder (otherwise "mods not found").
sourceSets.forEach {
    val dir = layout.buildDirectory.dir("sourceSets/${it.name}")
    it.output.setResourcesDir(dir)
    it.java.destinationDirectory = dir
}
