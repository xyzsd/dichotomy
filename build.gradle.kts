/*
 *  Copyright 2025, xyzsd (Zach Del)
 *
 *  Licensed under either of:
 *
 *    Apache License, Version 2.0
 *       (see LICENSE-APACHE or http://www.apache.org/licenses/LICENSE-2.0)
 *    MIT license
 *       (see LICENSE-MIT) or http://opensource.org/licenses/MIT)
 *
 *  at your option.
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("java-library")
    id("com.vanniktech.maven.publish") version "0.31.0"
    id("signing")
}


group = "net.xyzsd"
version = "1.1-SNAPSHOT"
// versions ending with "-SNAPSHOT" will end up at:
// https://central.sonatype.com/service/rest/repository/browse/maven-snapshots/net/xyzsd/dichotomy/
//

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    api("org.jspecify:jspecify:1.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }

    // withJavadocJar()
    // withSourcesJar()
}

tasks.compileJava {
    options.setEncoding("UTF-8")
    options.compilerArgs.add("-Xlint:preview")
    options.compilerArgs.add("-Xlint:unchecked")
}

tasks.test {
    useJUnitPlatform()
}

tasks.javadoc {
    val javadocOptions = options as CoreJavadocOptions
    //javadocOptions.addBooleanOption("-enable-preview", true)
    javadocOptions.addStringOption("source", "21")
    javadocOptions.addStringOption("Xdoclint:none", "-quiet")   // for sanity
    javadocOptions.addBooleanOption("html5", true)
}


// for reproducible builds
tasks.jar {
    setPreserveFileTimestamps(false);
    setReproducibleFileOrder(true);
}

// secrets: see
//   https://vanniktech.github.io/gradle-maven-publish-plugin/central/#secrets
//
// properties (local)
// ==================
//  mavenCentralUsername=[generated username (token)]
//  mavenCentralPassword=[token password, also generated]
//
// environmental (local or, say, Github)
// =====================================
//  ORG_GRADLE_PROJECT_mavenCentralUsername=[generated username (token)]
//  ORG_GRADLE_PROJECT_mavenCentralPassword=[token password, also generated]
//
mavenPublishing {
    configure(JavaLibrary(JavadocJar.Javadoc(), true))
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    pom {
        coordinates("net.xyzsd.dichotomy", "dichotomy", version as String)

        name.set("dichotomy")
        description.set("Result, Try, Maybe, and Either monads for Java")
        url.set("https://maven.pkg.github.com/xyzsd/dichotomy")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                comments.set("A business-friendly OSS license")
            }

            license {
                name.set("The MIT License")
                url.set("http://opensource.org/licenses/MIT")
                comments.set("A GPL/LGPL compatible OSS license")
            }
        }

        developers {
            developer {
                id.set("xyzsd")
                name.set("Zach Del")
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

signing {
    val githubCI: Boolean = "true".equals(System.getenv("CI")) || false;
    if (githubCI) {
        project.logger.lifecycle("Signing: Using Github environment.")
        val signingKey: String? = System.getenv("SIGNING_KEY_PRIVATE")
        val signingKeyPassphrase: String? = System.getenv("SIGNING_KEY_PASSPHRASE")
        useInMemoryPgpKeys(signingKey, signingKeyPassphrase)
    } else {
        project.logger.lifecycle("Signing: Using local credentials.")
    }
}




