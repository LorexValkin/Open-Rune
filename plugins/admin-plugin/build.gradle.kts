plugins {
    kotlin("jvm")
}

dependencies {
    compileOnly(project(":api"))
}

tasks.jar {
    archiveBaseName.set("admin-plugin")
}
