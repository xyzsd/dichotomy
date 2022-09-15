// see: 
//      https://docs.github.com/en/actions/publishing-packages/publishing-java-packages-with-gradle
//      https://docs.gradle.org/current/userguide/publishing_maven.html

plugins {
    id("java-library")
    id("maven-publish")
    signing
}

// TODO: move these to gradle.properties
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
        create<MavenPublication>("mavenJava") {
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
                
                scm {
                        connection.set("scm:git:git://github.com/xyzsd/dichotomy.git")
                        developerConnection.set("scm:git:ssh://git@github.com:xyzsd/dichotomy.git")
                        url.set("https://github.com/xyzsd/dichotomy")
                }
            }
        }
    }
    
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/xyzsd/dichotomy")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }  
        }
        
        // todo: maven{} section for OSSRH
    }
}

signing {
    val signingKey: String? = System.getenv("SIGNING_KEY_PRIVATE")
    val signingKeyPassphrase: String? = System.getenv("SIGNING_KEY_PASSPHRASE")
    println("SK:")
    println(signingKey)
    useInMemoryPgpKeys(signingKey, signingKeyPassphrase)
    sign(publishing.publications["mavenJava"])
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
