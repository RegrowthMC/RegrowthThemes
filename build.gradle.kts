plugins {
    `java-library`
    `maven-publish`
    id("com.gradleup.shadow") version("9.3.1")
    id("xyz.jpenilla.run-paper") version("3.0.2")
}

group = "org.lushplugins"
version = "1.2.2"

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
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.69-stable")
    compileOnly("com.github.retrooper:packetevents-spigot:2.13.0")

    // Soft Dependencies
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core:2.15.3")
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit:2.15.3") { isTransitive = false }

    // Libraries
    implementation("org.lushplugins:LushLib:0.10.35")
    implementation("dev.wyck:Wyck:3.0.0-6927904")
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
    }

    shadowJar {
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
        minecraftVersion("26.2")

        downloadPlugins {
            modrinth("fastasyncworldedit", "2.15.2")
            modrinth("packetevents", "2.13.0+spigot")
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
