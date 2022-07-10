plugins {
    id("chronolens.library-conventions")
}

dependencies {
    implementation(kotlin("reflect"))
    implementation("info.picocli:picocli:3.8.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.3")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.3")

    testImplementation(project(":chronolens-test"))
}
