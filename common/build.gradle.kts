plugins {
    id("blahaj-convention")
    id("net.neoforged.moddev") version "2.0.148"
}

// Compile-only module: vanilla Minecraft, no loader.
neoForge {
    neoFormVersion = "${property("minecraftVersion")}-1"
    accessTransformers.from("accesstransformer.cfg")
}

repositories {
    maven("https://maven.fabricmc.net/") { content { includeGroup("net.fabricmc") } }
}

dependencies {
    // Mixin annotations only; the loader provides Mixin at runtime.
    compileOnly("net.fabricmc:sponge-mixin:0.17.5+mixin.0.8.7")
    compileOnly("com.geckolib:geckolib-common-${property("minecraftVersion")}:${property("geckolibVersion")}")
}
