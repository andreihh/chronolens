plugins { id("chronolens.library-conventions") }

dependencies {
  // TODO: change to chronolens-api.
  implementation(project(":chronolens-core"))

  testImplementation(project(":chronolens-test"))
}
