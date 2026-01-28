plugins {
    id("asp.base-conventions")
    id("asp.publishing-conventions")
}

dependencies {
    compileOnly(project(":api"))

    api(libs.hikari)
    compileOnly(paperApi())

    testImplementation(project(":api"))
    testImplementation(libs.testcontainers.core)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.testcontainers.mysql)
    testImplementation(libs.slf4j.api)
    testRuntimeOnly("com.mysql:mysql-connector-j:9.1.0")
}

publishConfiguration {
    name = "Advanced Slime Paper MySQL Loader"
    description = "MySQL loader for Advanced Slime Paper"
}
