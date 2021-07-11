plugins {
    id("chronolens.library-conventions")
}

dependencies {
    api(project(":chronolens-core"))
    implementation("org.eclipse.jdt:org.eclipse.jdt.core:3.26.0")

    testImplementation(project(":chronolens-test"))
}
