plugins {
    `java-library`
    `maven-publish`
    id("io.github.goooler.shadow") version("8.1.7")
    id("xyz.jpenilla.run-paper") version("2.3.1")
}

group = "org.lushplugins"
version = "1.0.1"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.papermc.io/repository/maven-public/") // Paper, FastAsyncWorldEdit
    maven("https://repo.codemc.io/repository/maven-releases/") // PacketEvents
    maven("https://repo.lushplugins.org/snapshots") // LushLib
    maven("https://maven.enginehub.org/repo/") // FastAsyncWorldEdit
}

dependencies {
    // Dependencies
    compileOnly("io.papermc.paper:paper-api:1.21.3-R0.1-SNAPSHOT")
    compileOnly("com.github.retrooper:packetevents-spigot:2.6.0")

    // Soft Dependencies
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core:2.12.2")
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit:2.12.0") { isTransitive = false }

    // Libraries
    implementation("org.lushplugins:LushLib:0.10.19")
    implementation(platform("com.intellectualsites.bom:bom-newest:1.50")) // BOM: FastAsyncWorldEdit
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))

    registerFeature("optional") {
        usingSourceSet(sourceSets["main"])
    }

    withSourcesJar()
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    shadowJar {
        minimize()

        archiveFileName.set("${project.name}-${project.version}.jar")
    }

    processResources{
        filesMatching("plugin.yml") {
            expand(project.properties)
        }

        inputs.property("version", rootProject.version)
        filesMatching("plugin.yml") {
            expand("version" to rootProject.version)
        }
    }

    runServer {
        minecraftVersion("1.21")

        downloadPlugins {
            modrinth("fastasyncworldedit", "2.12.0")
            modrinth("packetevents", "QLgJReg5")
        }
    }
}

publishing {
    repositories {
        maven {
            name = "lushReleases"
            url = uri("https://repo.lushplugins.org/releases")
            credentials(PasswordCredentials::class)
            authentication {
                isAllowInsecureProtocol = true
                create<BasicAuthentication>("basic")
            }
        }

        maven {
            name = "lushSnapshots"
            url = uri("https://repo.lushplugins.org/snapshots")
            credentials(PasswordCredentials::class)
            authentication {
                isAllowInsecureProtocol = true
                create<BasicAuthentication>("basic")
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            groupId = rootProject.group.toString()
            artifactId = rootProject.name
            version = rootProject.version.toString()
            from(project.components["java"])
        }
    }
}
