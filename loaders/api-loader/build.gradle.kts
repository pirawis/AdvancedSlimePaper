plugins {
    id("asp.base-conventions")
    id("asp.publishing-conventions")
}

dependencies {
    compileOnly(project(":api"))
    compileOnly(paperApi())
    testImplementation(project(":api"))
    testRuntimeOnly(paperApi())
    testImplementation(libs.wiremock)
}

publishConfiguration {
    name = "Advanced Slime Paper API loader"
    description = "HTTP-API based loader for Advanced Slime Paper"
}
