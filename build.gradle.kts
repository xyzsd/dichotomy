plugins {
    id("java-library")
    id("maven-publish")
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

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("GitHubPackages") {
            from(components["java"])
            
            pom {
                name.set("dichotomy")
                description.set("Either and Result monads for Java")
                url.set("https://maven.pkg.github.com/xyzsd/dichotomy")   

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }

                    license {
                        name.set("The MIT License")
                        url.set("http://opensource.org/licenses/MIT")
                    }            
                }

                developers {
                    developer {
                        id.set("xyzsd")
                        email.set("xyzsd@xyzsd.net")
                    }
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["GitHubPackages"])
}

// JavaDoc options
tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}

// for reproducible builds
tasks.withType<AbstractArchiveTask>().configureEach {
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false
    dirMode = 775
    fileMode = 664
    archiveVersion.set("${project.version}")
}
