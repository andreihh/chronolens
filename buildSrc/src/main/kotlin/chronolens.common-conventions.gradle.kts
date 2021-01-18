// import java.net.URL
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
    id("com.diffplug.spotless")
    jacoco
}

group = "org.chronolens"
version = "0.2.11"

repositories {
    mavenCentral()
    jcenter()
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
        jvmTarget = "1.8"
        freeCompilerArgs += "-Xjvm-default=all"
    }
}

spotless {
    ratchetFrom("origin/master")

    kotlin {
        ktlint("0.40.0")
        licenseHeaderFile("$rootDir/spotless.kotlin.license")
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint("0.40.0")
    }
}

tasks.jar {
    from(rootProject.file("LICENSE"))
    from(rootProject.file("NOTICE"))
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)

    reports {
        html.isEnabled = true
        xml.isEnabled = true
        csv.isEnabled = false
    }
}

tasks.dokkaJavadoc {
    dokkaSourceSets {
        named("main") {
            // TODO: figure out why this leads to a superclass not found error.
            /*includes.from("Module.md")
            sourceLink {
                val ghRoot = "https://github.com/andreihh/chronolens"
                val ghProject = "$ghRoot/tree/master/${project.path}"
                val sourceRoot = "src/main/kotlin"

                localDirectory.set(file(sourceRoot))
                remoteUrl.set(URL("$ghProject/$sourceRoot"))
                remoteLineSuffix.set("#L")
            }*/
        }
    }
}

val sourcesJar by tasks.registering(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles sources Jar."
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

val javadocJar by tasks.registering(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles docs with Dokka."
    archiveClassifier.set("javadoc")
    from(tasks.dokkaJavadoc)
}

artifacts {
    archives(sourcesJar)
    archives(javadocJar)
}
