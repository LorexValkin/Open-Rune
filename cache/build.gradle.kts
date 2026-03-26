plugins {
    kotlin("jvm")
}

dependencies {
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("org.slf4j:slf4j-api:2.0.12")
    runtimeOnly("ch.qos.logback:logback-classic:1.5.6")
}

tasks.register<JavaExec>("dumpNpcs") {
    group = "tools"
    description = "Dump all NPC definitions from cache to Desktop"
    mainClass.set("com.openrune.cache.tools.NpcDumperKt")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("auditSpawns") {
    group = "tools"
    description = "Audit spawns.json against cache NPC definitions"
    mainClass.set("com.openrune.cache.tools.SpawnAuditorKt")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("exportCache") {
    group = "tools"
    description = "Export all cache definitions (NPCs, items, objects) to JSON"
    mainClass.set("com.openrune.cache.tools.CacheDataExporterKt")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("validateItems") {
    group = "tools"
    description = "Validate server items.json against cache item definitions and generate merged output"
    mainClass.set("com.openrune.cache.tools.ItemValidatorKt")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("exportEquipment") {
    group = "tools"
    description = "Export all equippable items from cache to data/equipment/equipment-cache.json"
    mainClass.set("com.openrune.cache.tools.EquipmentExporterKt")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("validateObjects") {
    group = "tools"
    description = "Validate tree/rock definitions against cache and generate cooking/smithing data"
    mainClass.set("com.openrune.cache.tools.ObjectValidatorKt")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("dumpMapObjects") {
    group = "tools"
    description = "Dump all map object placements from cache, export doors.json and summary report"
    mainClass.set("com.openrune.cache.tools.MapObjectDumperKt")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("verifyCacheDat2") {
    group = "tools"
    description = "Verify dat2 cache reader works against the rev 232 cache"
    mainClass.set("com.openrune.cache.tools.Dat2VerifierKt")
    classpath = sourceSets["main"].runtimeClasspath
}
