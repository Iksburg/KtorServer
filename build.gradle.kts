
plugins {
    kotlin("jvm") version "1.8.22"
    kotlin("plugin.serialization") version "1.9.10"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core:2.1.0")
    implementation("io.ktor:ktor-server-netty:2.1.0")
    implementation("io.ktor:ktor-server-auth:2.1.0")
    implementation("io.ktor:ktor-server-auth-jwt:2.1.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.1.0")
    implementation("io.ktor:ktor-server-content-negotiation:2.1.0")
    implementation("ch.qos.logback:logback-classic:1.5.18")
    implementation("org.jetbrains.exposed:exposed-core:0.41.1")
    implementation("org.jetbrains.exposed:exposed-java-time:0.41.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.41.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.41.1")
    implementation("org.postgresql:postgresql:42.3.4")
    implementation("com.auth0:java-jwt:3.18.1")
    implementation("at.favre.lib:bcrypt:0.9.0")
}

application {
    mainClass.set("com.example.ktorserver.ApplicationKt")
}
