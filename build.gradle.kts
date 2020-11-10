import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
        jcenter()
        maven("https://oss.sonatype.org/content/repositories/snapshots")
    }

    dependencies {
        classpath("com.android.tools.build:gradle:4.2.0-alpha16")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.0")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.3.0")
        classpath("com.google.gms:oss-licenses:0.9.2")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.28-alpha")
        // https://github.com/Triple-T/gradle-play-publisher/issues/864
        classpath("com.github.triplet.gradle:play-publisher:3.2.0-SNAPSHOT")
    }
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.14.1")
}

plugins {
    id("com.github.ben-manes.versions") version "0.34.0"
    id("se.eelde.build-optimizations") version "0.1.2"
    id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
    id("io.gitlab.arturbosch.detekt") version "1.14.2"
}

allprojects {
    repositories {
        google()
        mavenCentral()
        jcenter()
    }
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
