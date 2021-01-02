plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    jcenter()
}

dependencies {
    // Kotlin version embedded by Gradle 6.8-rc-4 is 1.4.20. We must not specify
    // a different version in order to avoid conflicts with the embedded
    // version. See https://youtrack.jetbrains.com/issue/KT-41142.
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.4.20")
}
