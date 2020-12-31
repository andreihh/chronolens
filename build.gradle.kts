import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    base
    jacoco
}

repositories {
    mavenCentral()
}

tasks.register<JacocoReport>("codeCoverageReport") {
    group = JavaBasePlugin.VERIFICATION_GROUP
    description = "Generates a combined code coverage report for all tests."

    executionData(
        fileTree(project.rootDir.absolutePath).include("**/build/jacoco/*.exec")
    )

    val relevantSubprojects =
        subprojects.filter {
            it.convention.getPlugins().contains("chronolens.common-conventions")
        }

    relevantSubprojects.forEach {
        dependsOn(it.tasks.named("jacocoTestReport"))
    }

    relevantSubprojects.forEach {
        sourceSets(it.the<SourceSetContainer>()["main"])
    }

    reports {
        html.isEnabled = true
        xml.isEnabled = true
        csv.isEnabled = false
    }
}
