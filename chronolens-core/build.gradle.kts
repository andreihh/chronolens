plugins { id("chronolens.library-conventions") }

dependencies {
  api(project(":chronolens-api"))
  implementation(kotlin("reflect"))
  implementation(libs.jackson.kotlin)
  implementation(libs.jackson.jsr310)

  testImplementation(project(":chronolens-test"))
}
