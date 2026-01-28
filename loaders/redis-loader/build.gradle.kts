plugins {
    id("asp.base-conventions")
    id("asp.publishing-conventions")
}

dependencies {
    compileOnly(project(":api"))

    api(libs.lettuce)

    compileOnly(paperApi())

    testImplementation(project(":api"))
    testImplementation(libs.testcontainers.core)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.testcontainers.redis)
    testImplementation(libs.slf4j.api)
}

publishConfiguration {
    name = "Advanced Slime Paper Redis Loader"
    description = "Redis loader for Advanced Slime Paper"
}
