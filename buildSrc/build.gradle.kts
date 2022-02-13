plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // implementation("gradle.plugin.com.github.spotbugs.snom:spotbugs-gradle-plugin:4.7.2")
    implementation("se.premex:ownership-gradle-plugin:0.0.7")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.19.0")
    implementation("com.android.tools.build:gradle:7.3.0-alpha02")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10")
    // javapoet is needed because it is transitively needed by hilt.
    implementation("com.squareup:javapoet:1.13.0")
}
