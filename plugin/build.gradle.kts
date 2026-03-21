import net.kyori.indra.git.IndraGitExtension

plugins {
    id("asp.base-conventions")
    id("asp.publishing-conventions")
    id("com.gradleup.shadow")
}

dependencies {
    compileOnly(project(":api"))
    implementation(project(":loaders"))

    implementation(libs.configurate.yaml)
    implementation(libs.bstats)
    implementation(libs.cloud.paper)
    implementation(libs.cloud.minecraft.extras)
    implementation(libs.cloud.annotations)

    compileOnly(paperApi())
    testImplementation(project(":api"))
    testImplementation(paperApi())
    testRuntimeOnly(paperApi())
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit.jupiter)
}

tasks {
    val generatePaperPluginDescription by registering {
        val outputFile = layout.buildDirectory.file("generated/plugin-metadata/paper-plugin.yml")
        val gitCommitId = providers.provider { project.the<IndraGitExtension>().commit()?.name ?: "unknown" }

        inputs.property("apiVersion", "1.21")
        inputs.property("name", "ASPaperPlugin")
        inputs.property("description", "ASP plugin for Paper, providing utilities for the ASP platform")
        inputs.property("mainClass", "com.infernalsuite.asp.plugin.SWPlugin")
        inputs.property("author", "InfernalSuite")
        inputs.property("gitCommitId", gitCommitId)
        outputs.file(outputFile)

        doLast {
            val file = outputFile.get().asFile
            file.parentFile.mkdirs()
            file.writeText(
                """
                api-version: "1.21"
                name: ASPaperPlugin
                version: ${gitCommitId.get()}
                main: com.infernalsuite.asp.plugin.SWPlugin
                description: "ASP plugin for Paper, providing utilities for the ASP platform"
                authors:
                  - InfernalSuite
                """.trimIndent() + System.lineSeparator(),
                Charsets.UTF_8
            )
        }
    }

    withType<Jar> {
        archiveBaseName.set("asp-plugin")
    }

    processResources {
        from(generatePaperPluginDescription)
    }

    shadowJar {
        archiveClassifier.set("")
        
        relocate("org.bstats", "com.infernalsuite.asp.libs.bstats")
        relocate("org.spongepowered.configurate", "com.infernalsuite.asp.libs.configurate")
        relocate("com.zaxxer.hikari", "com.infernalsuite.asp.libs.hikari")
        relocate("com.mongodb", "com.infernalsuite.asp.libs.mongo")
        relocate("io.lettuce", "com.infernalsuite.asp.libs.lettuce")
        relocate("org.bson", "com.infernalsuite.asp.libs.bson")
    }

    assemble {
        dependsOn(shadowJar)
    }
}
