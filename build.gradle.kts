plugins {
    kotlin("jvm") version "1.9.22" apply false
}

allprojects {
    group = "com.openrune"
    version = "0.1.0"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    dependencies {
        "implementation"(kotlin("stdlib"))
        "implementation"("org.slf4j:slf4j-api:2.0.12")
        "testImplementation"("org.junit.jupiter:junit-jupiter:5.10.2")
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
