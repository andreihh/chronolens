plugins { id("chronolens.application-conventions") }

application { mainClass.set("org.chronolens.cli.Main") }

dependencies {
  implementation(kotlin("reflect"))
  implementation(libs.kotlinx.cli)
  implementation(project(":chronolens-core"))
  runtimeOnly(project(":plugins:vcs:chronolens-git"))
  runtimeOnly(project(":plugins:parsers:chronolens-java"))
  runtimeOnly(project(":plugins:analyzers:chronolens-interactive"))
  runtimeOnly(project(":plugins:analyzers:chronolens-churn"))
  runtimeOnly(project(":plugins:analyzers:chronolens-coupling"))
  runtimeOnly(project(":plugins:analyzers:chronolens-decapsulations"))

  testImplementation(project(":chronolens-test"))
}
