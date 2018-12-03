import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.10")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:0.9.17")
    }
}

/*buildscript {
    ext {
        kotlin_version = '1.3.10'
        dokka_version = '0.9.17'
        jackson_version = '2.9.7'
        jackson_java8_version = '2.9.7'
        jdt_version = '3.13.102'
        junit_version = '4.12'
        picocli_version = '3.8.0'
        system_rules_version = '1.17.1'
    }

    repositories {
        mavenCentral()
        jcenter()
    }
}*/

plugins {
    kotlin("jvm") version "1.3.10" apply false
    id("org.jetbrains.dokka") version "0.9.17" apply false
    application
}

allprojects {
    group = "org.chronolens"
    version = "0.2.11"

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "jacoco")

    //sourceCompatibility = JavaVersion.VERSION_1_8
    //targetCompatibility = JavaVersion.VERSION_1_8

    repositories {
        mavenCentral()
        jcenter()
    }

    dependencies {
        implementation(kotlin("stdlib-jdk8"))

        testImplementation(kotlin("test-junit"))
        testImplementation("junit:junit:4.12")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    tasks.test {
        workingDir = createTempDir().apply(File::deleteOnExit)
    }

    tasks.jar {
        from(rootProject.file("LICENSE"))
        from(rootProject.file("NOTICE"))
    }

    tasks.named<JacocoReport>("jacocoTestReport") {
        dependsOn(tasks.test)

        reports {
            html.isEnabled = true
            xml.isEnabled = true
            csv.isEnabled = false
        }
    }
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "org.jetbrains.dokka")

    val dokka by tasks.existing(DokkaTask::class) {
        moduleName = project.name
        includes = listOf("Module.md")
        impliedPlatforms = arrayListOf("JVM")
        jdkVersion = 8
        outputFormat = "javadoc"

        /*linkMapping {
            dir = "src/main/kotlin"
            url = "https://github.com/andreihh/${rootProject.name}/blob/master/${project.name}/$dir"
            suffix = "#L"
        }*/
    }

    val sourcesJar by tasks.registering(Jar::class) {
        group = JavaBasePlugin.DOCUMENTATION_GROUP
        description = "Assembles sources Jar."
        classifier = "sources"
        from(sourceSets.getByName("main").allSource)
    }

    val javadocJar by tasks.registering(Jar::class) {
        group = JavaBasePlugin.DOCUMENTATION_GROUP
        description = "Assembles docs with Dokka."
        classifier = "javadoc"
        from(dokka)
    }

    artifacts {
        add("archives", sourcesJar)
        add("archives", javadocJar)
    }
}

application {
    mainClassName = "org.chronolens.Main"
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

tasks.register<JacocoReport>("codeCoverageReport") {
    group = JavaBasePlugin.VERIFICATION_GROUP
    description = "Generates a combined code coverage report for all tests."

    allprojects.forEach {
        dependsOn(it.tasks.named("jacocoTestReport"))
    }

    executionData(
        fileTree(project.rootDir.absolutePath).include("**/build/jacoco/*.exec")
    )

    allprojects.forEach {
        sourceSets(it.sourceSets.getByName("main"))
    }

    reports {
        html.isEnabled = true
        xml.isEnabled = true
        csv.isEnabled = false
    }
}
