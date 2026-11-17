plugins { java }

val modId = property("modId") as String
val jdk = (property("jdkVersion") as String).toInt()
group = property("modGroup") as String
version = property("modVersion") as String
base.archivesName = "blahaj-reborn-${project.name}-${property("minecraftVersion")}"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(jdk))
    withSourcesJar()
}

repositories {
    exclusiveContent {
        forRepository {
            maven("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/") { name = "GeckoLib" }
        }
        filter { includeGroupAndSubgroups("com.geckolib") }
    }
    mavenCentral()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(jdk)
}

// Loader modules compile :common's sources and resources directly (no intermediate jar).
if (project.name != "common") {
    val common = project(":common")
    afterEvaluate {
        sourceSets.main {
            java.srcDir(common.file("src/main/java"))
            resources.srcDir(common.file("src/main/resources"))
        }
    }
}

// Metadata files contain non-ASCII characters (Blåhaj): never rely on the platform charset.
tasks.withType<ProcessResources>().configureEach {
    filteringCharset = "UTF-8"
}
