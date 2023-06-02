plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")


    val scoreboardLibraryVersion = "2.0.0-RC8"
    implementation("com.github.megavexnetwork.scoreboard-library:scoreboard-library-api:$scoreboardLibraryVersion")
    implementation("com.github.megavexnetwork.scoreboard-library:scoreboard-library-extra-kotlin:$scoreboardLibraryVersion") // If using Kotlin
    runtimeOnly("com.github.megavexnetwork.scoreboard-library:scoreboard-library-implementation:$scoreboardLibraryVersion")

    // Add packet adapter implementations you want:
    runtimeOnly("com.github.megavexnetwork.scoreboard-library:scoreboard-library-v1_8_R3:$scoreboardLibraryVersion")
    runtimeOnly("com.github.megavexnetwork.scoreboard-library:scoreboard-library-v1_19_R3:$scoreboardLibraryVersion")
    runtimeOnly("com.github.megavexnetwork.scoreboard-library:scoreboard-library-packetevents:$scoreboardLibraryVersion")
    implementation("net.kyori:adventure-platform-bukkit:4.0.1")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

