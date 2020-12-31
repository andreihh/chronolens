import java.io.File
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

    val modules =
        subprojects.filter { it.tasks.findByName("jacocoTestReport") != null }

    modules.forEach {
        val reports = it.tasks.withType<JacocoReport>()
        val sources = it.the<SourceSetContainer>()["main"]
        //val data = reports.flatMap(JacocoReport::executionData)

        dependsOn(reports)
        sourceSets(sources)
        //executionData(data)
    }

    reports {
        html.isEnabled = true
        xml.isEnabled = true
        csv.isEnabled = false
    }
}
