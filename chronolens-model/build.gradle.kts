plugins { id("chronolens.library-conventions") }

dependencies {
  testImplementation(project(":chronolens-test"))
  // TODO: remove this dependency.
  testImplementation(project(":chronolens-core"))
}
