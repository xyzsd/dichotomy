// see: 
//      https://docs.github.com/en/actions/publishing-packages/publishing-java-packages-with-gradle
//      https://docs.gradle.org/current/userguide/publishing_maven.html

plugins {
    id("java-library")
    id("maven-publish")
    signing
}

group = "net.xyzsd"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains:annotations:24.0.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java { 
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(20))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }

    withJavadocJar()
    withSourcesJar()
}


tasks.getByName<Test>("test") {
    useJUnitPlatform()
}



tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Xlint:preview")
    options.compilerArgs.add("-Xlint:unchecked")
}


tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.withType<JavaExec>().configureEach {
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
            name = "OSSRH"
            
            val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots/")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
        
            credentials {
                    username = System.getenv("OSSRH_USERNAME")
                    password = System.getenv("OSSRH_PASSWORD")
                }
        }
    }
}

signing {
    val signingKey: String? = System.getenv("SIGNING_KEY_PRIVATE")
    val signingKeyPassphrase: String? = System.getenv("SIGNING_KEY_PASSPHRASE")
    useInMemoryPgpKeys(signingKey, signingKeyPassphrase)
    sign(publishing.publications["mavenJava"])
}

// JavaDoc options
tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
    val javadocOptions = options as CoreJavadocOptions
    javadocOptions.addStringOption("source", "20")
}

// for reproducible builds
tasks.withType<AbstractArchiveTask>().configureEach {
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false
    dirMode = 775
    fileMode = 664
    archiveVersion.set("${project.version}")
}
