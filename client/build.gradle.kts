plugins {
    id("java")
    id("application")
}

application {
    mainClass.set("client")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

sourceSets {
    main {
        java {
            srcDirs("src/main/java")
        }
    }
}
