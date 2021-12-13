import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
        jcenter()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.0.4")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.0")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.4.0-beta02")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.40.5")
        classpath("com.google.gms:google-services:4.3.10")
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.18.0")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.6.0")
        classpath("com.google.android.gms:oss-licenses-plugin:0.10.4")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.8.1")
    }
}

plugins {
    id("com.github.ben-manes.versions") version "0.39.0"
    // id("se.eelde.build-optimizations") version "0.2.0"
    id("io.gitlab.arturbosch.detekt") version "1.19.0"
    // https://github.com/Kotlin/KEEP/blob/master/proposals/explicit-api-mode.md
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.8.0"
    id("com.github.triplet.play") version "3.7.0" apply false
}

allprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    dependencies {
        detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.19.0")
    }
    detekt {
        autoCorrect = true
    }

    repositories {
        google()
        mavenCentral()
        jcenter()
    }
}

apiValidation {
    ignoredProjects.add("toggles-app")
    ignoredProjects.add("toggles-sample")
}

detekt {
    autoCorrect = true
    buildUponDefaultConfig = true
    config = files("$projectDir/config/detekt/detekt.yml")
    baseline = file("$projectDir/config/detekt/baseline.xml")

    reports {
        html.enabled = true
    }
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
