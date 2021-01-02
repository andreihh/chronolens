plugins {
    id("chronolens.common-conventions")
    `java-library`
    `maven-publish`
}

kotlin {
    // TODO: switch to strict 'explicitApi()' once all warnings are fixed.
    explicitApiWarning()
}
