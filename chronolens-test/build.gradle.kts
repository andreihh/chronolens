plugins { id("chronolens.library-conventions") }

dependencies {
  api(project(":chronolens-api"))
  implementation(kotlin("test"))
  implementation(kotlin("test-junit"))
  implementation(libs.junit)
  runtimeOnly(libs.kotlin.jsr223)
}
