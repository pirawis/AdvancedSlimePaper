plugins {
    id("asp.base-conventions")
    id("asp.publishing-conventions")
}

dependencies {
    compileOnly(project(":api"))
    compileOnly(paperApi())
    testImplementation(project(":api"))
    testRuntimeOnly(paperApi())
}

publishConfiguration {
    name = "Advanced Slime Paper File Loader"
    description = "File loader for Advanced Slime Paper"
}
