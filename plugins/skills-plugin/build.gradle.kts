plugins {
    kotlin("jvm")
}

dependencies {
    compileOnly(project(":api"))
    implementation("com.google.code.gson:gson:2.11.0")
}

tasks.jar {
    archiveBaseName.set("skills-plugin")
}
