plugins {
    kotlin("jvm")
    application
}

application {
    mainClass.set("com.openrune.launcher.LauncherKt")
}

dependencies {
    api(project(":api"))
    api(project(":core"))
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("ch.qos.logback:logback-classic:1.5.3")
}

tasks.named<JavaExec>("run") {
    workingDir = rootProject.projectDir
}
