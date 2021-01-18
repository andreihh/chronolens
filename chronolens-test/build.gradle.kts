plugins {
    id("chronolens.library-conventions")
}

dependencies {
    api(project(":chronolens-core"))
    implementation(kotlin("test"))
    implementation(kotlin("test-junit"))
    implementation("junit:junit:4.13")
}
