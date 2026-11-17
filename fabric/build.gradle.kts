plugins {
    id("blahaj-convention")
    id("net.fabricmc.fabric-loom") version "1.17-SNAPSHOT"
}

repositories {
    exclusiveContent {
        forRepository { maven("https://api.modrinth.com/maven") { name = "Modrinth" } }
        filter { includeGroup("maven.modrinth") }
    }
}

// Shader compatibility test: ./gradlew :fabric:runClientGameTest -Pshaders=true
// (Iris + Sodium are test-runtime only, never part of the mod jar.)
val testWithShaders = providers.gradleProperty("shaders").map { it.toBoolean() }.getOrElse(false)

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraftVersion")}")
    implementation("net.fabricmc:fabric-loader:${property("fabricLoaderVersion")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${property("fabricApiVersion")}")
    implementation("com.geckolib:geckolib-fabric-${property("minecraftVersion")}:${property("geckolibVersion")}")

    if (testWithShaders) {
        runtimeOnly("maven.modrinth:iris:1.11.7+26.3-fabric")
        runtimeOnly("maven.modrinth:sodium:mc26.3-0.9.2-fabric")
    }
}

// With -Pshaders=true: copies the shader pack into the test run and enables it.
tasks.matching { it.name == "runClientGameTest" }.configureEach {
    doFirst {
        if (testWithShaders) {
            val runDir = file("build/run/clientGameTest")
            copy { from("run-shaderpacks"); into(runDir.resolve("shaderpacks")) }
            val pack = file("run-shaderpacks").listFiles()?.firstOrNull { it.name.endsWith(".zip") }?.name ?: ""
            runDir.resolve("config").mkdirs()
            runDir.resolve("config/iris.properties").writeText("enableShaders=true" + System.lineSeparator() + "shaderPack=" + pack + System.lineSeparator())
        }
    }
}

fabricApi {
    configureTests {
        createSourceSet = true
        modId = "blahaj-gametest"
        enableGameTests = false
    }
}

loom {
    runs {
        named("client") {
            client()
            runDir("runs/client")
        }
        named("server") {
            server()
            runDir("runs/server")
        }
    }
}

val props = mapOf(
    "version" to project.version,
    "mod_id" to property("modId"),
    "mod_name" to property("modName"),
    "mod_authors" to property("modAuthors"),
    "mod_description" to property("modDescription"),
    "minecraft_version" to property("minecraftVersion"),
    "java_version" to property("jdkVersion"),
    "geckolib_version" to property("geckolibVersion"),
)

tasks.processResources {
    inputs.properties(props)
    filesMatching("fabric.mod.json") { expand(props) }
}
