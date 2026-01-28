plugins {
    id("asp.base-conventions")
    id("asp.internal-conventions")
    id("asp.publishing-conventions")
}

dependencies {
    compileOnly(project(":api"))
    compileOnly(paperApi())
    implementation(libs.zstd)

    testImplementation(project(":api"))
    testImplementation(paperApi())
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit.jupiter)
}

publishConfiguration {
    name = "Advanced Slime Paper Core"
    description = "Core logic for Advanced Slime Paper"
}
