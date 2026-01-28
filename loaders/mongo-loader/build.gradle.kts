plugins {
    id("asp.base-conventions")
    id("asp.publishing-conventions")
}

dependencies {
    compileOnly(project(":api"))
    compileOnly(paperApi())

    api(libs.mongo)

    testImplementation(project(":api"))
    testImplementation(libs.testcontainers.core)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.testcontainers.mongodb)
    testImplementation(libs.slf4j.api)
}

publishConfiguration {
    name = "Advanced Slime Paper MongoDB Loader"
    description = "MongoDB GridFS Loader for Advanced Slime Paper"
}
