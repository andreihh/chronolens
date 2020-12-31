plugins {
    id("chronolens.application-conventions")
}

application {
    mainClass.set("org.chronolens.Main")
}

dependencies {
    implementation(project(":chronolens-core"))
    runtimeOnly(project(":services:chronolens-git"))
    runtimeOnly(project(":services:chronolens-java"))
    runtimeOnly(project(":analyzers:chronolens-churn"))
    runtimeOnly(project(":analyzers:chronolens-coupling"))
    runtimeOnly(project(":analyzers:chronolens-decapsulations"))

    testImplementation(project(":chronolens-test"))
    testImplementation("com.github.stefanbirkner:system-rules:1.17.1")
}

