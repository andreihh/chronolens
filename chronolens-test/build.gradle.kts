plugins { id("chronolens.library-conventions") }

dependencies {
  api(project(":chronolens-api"))
  // TODO: remove this dependency.
  implementation(project(":chronolens-core"))
  implementation(kotlin("test"))
  implementation(kotlin("test-junit"))
  implementation(libs.junit)
}
