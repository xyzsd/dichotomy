plugins {
    id("java-library")
}

group = "net.xyzsd"
version = "0.8-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains:annotations:23.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}


// for reproducible builds
tasks.withType<AbstractArchiveTask>().configureEach {
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false
    dirMode = 775
    fileMode = 664
    archiveVersion.set("${project.version}")
}