plugins { id("chronolens.library-conventions") }

dependencies {
  implementation(project(":chronolens-api"))

  testImplementation(project(":chronolens-test"))
}
