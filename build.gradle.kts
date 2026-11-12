import java.io.BufferedReader
import java.io.InputStreamReader

plugins {
    `java-library`
    id("com.gradleup.shadow") version("9.3.1")
    id("xyz.jpenilla.run-paper") version("3.1.0")
}

group = "org.lushplugins"
version = "2.0.0"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.papermc.io/repository/maven-public/") // Paper, FastAsyncWorldEdit
    maven("https://repo.codemc.io/repository/maven-releases/") // PacketEvents
    maven("https://repo.wyck.dev/snapshots") // Wyck
    maven("https://repo.lushplugins.org/snapshots") // LushLib
    maven("https://maven.enginehub.org/repo/") // FastAsyncWorldEdit
}

dependencies {
    // Dependencies
    compileOnly("io.papermc.paper:paper-api:26.3.build.+")
    compileOnly("com.github.retrooper:packetevents-spigot:2.14.0")

    // Soft Dependencies
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core:2.16.1")
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit:2.16.2") { isTransitive = false }

    // Libraries
    implementation("org.lushplugins:LushLib:1.0.2")
    implementation("io.github.revxrsal:lamp.common:4.0.1")
    implementation("io.github.revxrsal:lamp.bukkit:4.0.1")
    implementation("dev.wyck:Wyck:4.0.0-837d6ec")
    implementation(platform("com.intellectualsites.bom:bom-newest:1.56")) // BOM: FastAsyncWorldEdit
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))

    registerFeature("optional") {
        usingSourceSet(sourceSets["main"])
    }

    withSourcesJar()
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }

    shadowJar {
        enableAutoRelocation = true
        relocationPrefix = "org.lushplugins.regrowththemes.libraries"

        archiveFileName.set("${project.name}-${project.version}.jar")
    }

    processResources{
        inputs.property("version", rootProject.version)
        inputs.property("commit", getCurrentCommitHash())

        filesMatching("plugin.yml") {
            expand(
                "version" to rootProject.version,
                "commit" to getCurrentCommitHash()
            )
        }
    }

    runServer {
        minecraftVersion("26.3")

        downloadPlugins {
            modrinth("fastasyncworldedit", "2.16.2")
            modrinth("packetevents", "2.14.0+spigot")
        }
    }
}

fun getCurrentCommitHash(): String {
    val process = ProcessBuilder("git", "rev-parse", "--short", "HEAD").start()
    val reader = BufferedReader(InputStreamReader(process.inputStream))
    val commitHash = reader.readLine()
    reader.close()
    process.waitFor()
    if (process.exitValue() == 0) {
        return commitHash ?: ""
    } else {
        throw IllegalStateException("Failed to retrieve the commit hash.")
    }
}
