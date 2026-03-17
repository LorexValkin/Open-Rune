plugins {
    kotlin("jvm")
    application
}

application {
    mainClass.set("Jframe")
    applicationDefaultJvmArgs = listOf(
        "-Dopenrune.server=127.0.0.1",
        "-Dopenrune.port=43594",
        "-Dsun.java2d.opengl=false"
    )
}

// Client sources are Java (317 client), not Kotlin
sourceSets {
    main {
        java {
            srcDir("src/main/java")
        }
        // No Kotlin sources in this module
        kotlin {
            setSrcDirs(emptyList<String>())
        }
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf(
        "-Xlint:none"  // Suppress warnings on legacy code
    ))
}

dependencies {
    implementation("ch.qos.logback:logback-classic:1.5.3")
    implementation("org.slf4j:slf4j-api:2.0.12")
}

tasks.named<JavaExec>("run") {
    workingDir = rootProject.projectDir
    standardInput = System.`in`
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "Jframe"
    }
    archiveBaseName.set("openrune-client")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}
