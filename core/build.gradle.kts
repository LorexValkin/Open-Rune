plugins {
    kotlin("jvm")
    application
}

application {
    mainClass.set("com.openrune.core.ServerKt")
}

dependencies {
    api(project(":api"))
    api(project(":cache"))

    // Networking
    implementation("io.netty:netty-all:4.1.108.Final")

    // JSON
    implementation("com.google.code.gson:gson:2.11.0")

    // Logging impl
    implementation("ch.qos.logback:logback-classic:1.5.3")

    // Kotlin coroutines for async plugin loading
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.openrune.core.ServerKt"
    }
}

tasks.named<JavaExec>("run") {
    workingDir = rootProject.projectDir
}
