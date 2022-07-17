// import java.net.URL
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("com.diffplug.spotless")
    jacoco
}

group = "org.chronolens"
version = "0.2.12"

repositories {
    mavenCentral()
}

// Set versions for common dependencies.
val junitVersion by extra("4.13")

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    testImplementation(kotlin("test-junit"))
    testImplementation("junit:junit:$junitVersion")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        languageVersion = "1.7"
        jvmTarget = "11"
        freeCompilerArgs += "-Xjvm-default=all"
    }
}

spotless {
    ratchetFrom("origin/master")

    kotlin {
        ktfmt("0.39").kotlinlangStyle()
        licenseHeaderFile("$rootDir/spotless.kotlin.license")
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktfmt("0.39").kotlinlangStyle()
    }
}

tasks.jar {
    from(rootProject.file("LICENSE"))
    from(rootProject.file("NOTICE"))
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)

    reports {
        html.required.set(true)
        xml.required.set(true)
        csv.required.set(false)
    }
}

val sourcesJar by tasks.registering(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles sources Jar."
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

artifacts {
    archives(sourcesJar)
}
