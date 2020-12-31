plugins {
    id("chronolens.library-conventions")
}

dependencies {
    api(project(":chronolens-core"))
    implementation(kotlin("test"))
}
