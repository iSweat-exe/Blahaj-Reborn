pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
            content { includeGroupAndSubgroups("net.fabricmc"); includeGroup("fabric-loom") }
        }
        maven("https://maven.neoforged.net/releases") {
            name = "NeoForge"
            content { includeGroupAndSubgroups("net.neoforged"); includeGroup("codechicken"); includeGroup("net.covers1624") }
        }
        maven("https://maven.minecraftforge.net/") {
            name = "Forge"
            content { includeGroupAndSubgroups("net.minecraftforge") }
        }
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "blahaj-reborn"

include("common")
include("fabric")
include("neoforge")
include("forge")
