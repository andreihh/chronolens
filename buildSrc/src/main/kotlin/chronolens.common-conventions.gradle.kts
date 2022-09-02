import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URL

plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
    jacoco
    id("com.diffplug.spotless")
}

group = "org.chronolens"
version = "0.2.13"

repositories {
    mavenCentral()
}

// Set versions for common dependencies, as library catalogs are not yet
// avalable in buildSrc implementation files.
val junitVersion by extra("4.13.2")
val ktfmtVersion by extra("0.40")

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

tasks.withType<DokkaTask> {
    dokkaSourceSets.configureEach {
        includes.from("Module.md")
        sourceLink {
            val ghProject = "https://github.com/andreihh/chronolens/tree/master/${project.path}"
            val sourceRoot = "src/main/kotlin"

            localDirectory.set(file(sourceRoot))
            remoteUrl.set(URL("$ghProject/$sourceRoot"))
            remoteLineSuffix.set("#L")
        }
    }
}

spotless {
    ratchetFrom("origin/master")

    kotlin {
        ktfmt(ktfmtVersion).kotlinlangStyle()
        licenseHeaderFile("$rootDir/spotless.kotlin.license")
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktfmt(ktfmtVersion).kotlinlangStyle()
    }
}

tasks.jar {
    from(rootProject.file("LICENSE"))
    from(rootProject.file("NOTICE"))
}

val sourcesJar by tasks.registering(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles sources Jar."
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

val javadocJar by tasks.registering(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles Javadocs Jar with Dokka."
    archiveClassifier.set("javadoc")
    from(tasks.dokkaJavadoc)
}

artifacts {
    archives(sourcesJar)
    archives(javadocJar)
}
