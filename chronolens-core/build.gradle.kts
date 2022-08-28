plugins { id("chronolens.library-conventions") }

dependencies {
    implementation(kotlin("reflect"))
    implementation(libs.picocli)
    implementation(libs.jackson.kotlin)
    implementation(libs.jackson.jsr310)

    testImplementation(project(":chronolens-test"))
}
