import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    java
    id("io.papermc.paperweight.patcher")
}

paperweight {
    upstreams.paper {
        ref = providers.gradleProperty("paperRef")

        // Setup file patches for build scripts
        patchFile {
            path = "paper-api/build.gradle.kts"
            outputFile = file("aspaper-api/build.gradle.kts")
            patchFile = file("aspaper-api/build.gradle.kts.patch")
        }
        patchFile {
            path = "paper-server/build.gradle.kts"
            outputFile = file("aspaper-server/build.gradle.kts")
            patchFile = file("aspaper-server/build.gradle.kts.patch")
        }

        patchDir("paperApi") {
            upstreamPath = "paper-api"
            excludes = setOf("build.gradle.kts")
            patchesDir = file("aspaper-api/paper-patches")
            outputDir = file("paper-api")
        }
    }
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(JAVA_VERSION)
        }
    }

    repositories {
        mavenCentral()
        maven(PAPER_MAVEN_PUBLIC_URL)
    }

    configurations.all {
        resolutionStrategy {
            force("at.yawk.lz4:lz4-java:1.10.3")
            force("org.apache.commons:commons-lang3:3.18.0")
            force("org.eclipse.jgit:org.eclipse.jgit:7.2.1.202505142326-r")
            force("com.google.protobuf:protobuf-java:4.28.2")
            force("com.fasterxml.jackson.core:jackson-core:2.15.0")
            force("com.fasterxml.jackson.core:jackson-databind:2.15.0")
            force("com.fasterxml.jackson.core:jackson-annotations:2.15.0")
            force("io.netty:netty-handler:4.1.125.Final")
            force("io.netty:netty-common:4.1.125.Final")
            force("io.netty:netty-codec:4.1.125.Final")
            force("io.netty:netty-buffer:4.1.125.Final")
            force("io.netty:netty-transport:4.1.125.Final")
            force("io.netty:netty-resolver:4.1.125.Final")
            force("org.yaml:snakeyaml:2.0")
            force("com.mysql:mysql-connector-j:9.3.0")
            force("io.github.classgraph:classgraph:4.8.112")
            force("org.apache.logging.log4j:log4j-core:2.25.3")
            force("org.apache.logging.log4j:log4j-api:2.25.3")
            force("com.nimbusds:nimbus-jose-jwt:10.0.2")
            force("net.minidev:json-smart:2.5.2")
        }
    }

    tasks.withType<AbstractArchiveTask>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
    tasks.withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        options.release = JAVA_VERSION
        options.isFork = true
        options.compilerArgs.add("-Xlint:none")
    }
    tasks.withType<Javadoc> {
        options.encoding = Charsets.UTF_8.name()
        (options as StandardJavadocDocletOptions).apply {
            addStringOption("Xdoclint:none", "-quiet")
            addStringOption("-Xmaxwarns", "1")
        }
        isFailOnError = false
        logging.captureStandardError(LogLevel.QUIET)
        logging.captureStandardOutput(LogLevel.QUIET)
    }
    tasks.withType<ProcessResources> {
        filteringCharset = Charsets.UTF_8.name()
    }
    tasks.withType<Test> {
        testLogging {
            showStackTraces = false
            exceptionFormat = TestExceptionFormat.SHORT
            events()
        }
    }
}
