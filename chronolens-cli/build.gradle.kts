plugins { id("chronolens.application-conventions") }

application { mainClass.set("org.chronolens.cli.Main") }

dependencies {
    implementation(project(":chronolens-core"))
    implementation(libs.kotlinx.cli)
    runtimeOnly(project(":services:chronolens-git"))
    runtimeOnly(project(":services:chronolens-java"))
    runtimeOnly(project(":analyzers:chronolens-interactive"))
    runtimeOnly(project(":analyzers:chronolens-churn"))
    runtimeOnly(project(":analyzers:chronolens-coupling"))
    runtimeOnly(project(":analyzers:chronolens-decapsulations"))

    testImplementation(project(":chronolens-test"))
    testImplementation(libs.systemrules)
}
