plugins {
    `java-library`
    id("net.kyori.indra.git")
    jacoco
}

group = rootProject.providers.gradleProperty("group").get()
version = rootProject.providers.gradleProperty("apiVersion").get()

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(JAVA_VERSION))
    }
    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenLocal()
    mavenCentral()

    maven(PAPER_MAVEN_PUBLIC_URL)

    maven("https://repo.codemc.io/repository/nms/")
    maven("https://repo.rapture.pw/repository/maven-releases/")
    maven("https://repo.glaremasters.me/repository/concuncan/")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
//    api(platform(project(":gradle:platform")))

    // Test dependencies
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit.jupiter)
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(JAVA_VERSION)
    }

    javadoc {
        options.encoding = Charsets.UTF_8.name()
        (options as StandardJavadocDocletOptions)
            .tags("apiNote:a:API Note", "implSpec:a:Implementation Requirements", "implNote:a:Implementation Note")
    }

    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
            showExceptions = true
            showCauses = true
            showStackTraces = true
        }
        reports {
            junitXml.required.set(true)
            html.required.set(true)
        }
        finalizedBy(jacocoTestReport)
    }

    jacocoTestReport {
        dependsOn(test)
        reports {
            xml.required.set(true)
            html.required.set(true)
            csv.required.set(false)
        }
    }

    jacocoTestCoverageVerification {
        violationRules {
            rule {
                limit {
                    minimum = "0.0".toBigDecimal() // Start with 0, increase over time
                }
            }
        }
    }
}

jacoco {
    toolVersion = "0.8.12"
}
