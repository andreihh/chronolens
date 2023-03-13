plugins { id("chronolens.library-conventions") }

dependencies {
  api(project(":chronolens-model"))

  testImplementation(project(":chronolens-test"))
}
