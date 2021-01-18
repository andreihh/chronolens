plugins {
    id("chronolens.library-conventions")
}

val junitVersion: String by extra

dependencies {
    api(project(":chronolens-core"))
    implementation(kotlin("test"))
    implementation(kotlin("test-junit"))
    implementation("junit:junit:$junitVersion")
}
