import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.3.0-alpha02")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10")
        // classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.4.0-rc01")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.40.5")
        classpath("com.google.gms:google-services:4.3.13")
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.20.0")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.7.0")
        classpath("com.google.android.gms:oss-licenses-plugin:0.10.4")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.1")
        classpath("com.squareup:javapoet:1.13.0")

    }
}

val composeVersion by extra("1.2.0-alpha03")

plugins {
    id("com.github.ben-manes.versions") version "0.42.0"
    // id("se.eelde.build-optimizations") version "0.2.0"
    // https://github.com/Kotlin/KEEP/blob/master/proposals/explicit-api-mode.md
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.10.1"
    id("com.github.triplet.play") version "3.7.0" apply false
    id("toggles.ownership-conventions")
}

apiValidation {
    ignoredProjects.add("toggles-app")
    ignoredProjects.add("toggles-sample")
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

tasks.named("dependencyUpdates", DependencyUpdatesTask::class.java).configure {
    rejectVersionIf {
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}
