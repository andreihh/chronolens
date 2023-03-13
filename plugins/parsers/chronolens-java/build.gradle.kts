plugins { id("chronolens.library-conventions") }

dependencies {
  implementation(project(":chronolens-api"))
  implementation(libs.eclipse.jdt)

  testImplementation(project(":chronolens-test"))
}
