plugins {
    base
    jacoco
}

repositories {
    mavenCentral()
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}

val Project.jacocoReportTasks get() = tasks.withType<JacocoReport>()

tasks.register<JacocoReport>("codeCoverageReport") {
    group = JavaBasePlugin.VERIFICATION_GROUP
    description = "Generates a combined code coverage report for all tests."

    executionData(
        fileTree(project.rootDir.absolutePath).include("**/build/jacoco/*.exec")
    )

    subprojects
        .filter { it.jacocoReportTasks.isNotEmpty() }
        .forEach {
            dependsOn(it.jacocoReportTasks)
            sourceSets(it.the<SourceSetContainer>()["main"])
        }

    reports {
        html.isEnabled = true
        xml.isEnabled = true
        csv.isEnabled = false
    }
}
