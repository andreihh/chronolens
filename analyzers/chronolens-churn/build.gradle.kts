plugins { id("chronolens.library-conventions") }

dependencies {
    api(project(":chronolens-core"))

    testImplementation(project(":chronolens-test"))
}
