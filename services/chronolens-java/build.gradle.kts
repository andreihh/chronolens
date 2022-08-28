plugins { id("chronolens.library-conventions") }

dependencies {
    api(project(":chronolens-core"))
    implementation(libs.eclipse.jdt)

    testImplementation(project(":chronolens-test"))
}
