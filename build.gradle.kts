import org.jetbrains.gradle.ext.*
import org.jetbrains.gradle.ext.Application

plugins {
    id("java")
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.7"
}

group = "github.io.chaosunity.xikou"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
        // Azul covers the most platforms for Java 8 toolchains, crucially including MacOS arm64
        vendor.set(JvmVendorSpec.AZUL)
    }
    // Generate sources and javadocs jars when building and publishing
    withSourcesJar()
    // withJavadocJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.ow2.asm:asm:9.6")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

idea {
    module {
        inheritOutputDirs = true
    }
    project {
        settings {
            runConfigurations {
                add(Application("Compile Self Hosting Compiler", project).apply {
                    mainClass = "github.io.chaosunity.xikou.Main"
                    moduleName = "${project.idea.module.name}.main"
                    programParameters = "self_host output"
                })
                add(Application("Compile Example", project).apply {
                    mainClass = "github.io.chaosunity.xikou.Main"
                    moduleName = "${project.idea.module.name}.main"
                    programParameters = "example output"
                })
            }
            compiler.javac {
                afterEvaluate {
                    javacAdditionalOptions = "-encoding utf8"
                }
            }
        }
    }
}
