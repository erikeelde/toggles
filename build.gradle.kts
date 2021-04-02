import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {

    val roomVersion by extra("2.3.0-rc01")
    val composeVersion by extra("1.0.0-beta03")
    val lifecycleVersion by extra("2.3.1")

    repositories {
        google()
        mavenCentral()
        jcenter()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.0.0-alpha12")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.31")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.3.4")
        classpath("com.google.gms:oss-licenses:0.9.2")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.33-beta")
        classpath("com.google.gms:google-services:4.3.5")
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.14.2")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.4.30")
    }
}

plugins {
    id("com.github.ben-manes.versions") version "0.38.0"
    id("se.eelde.build-optimizations") version "0.2.0"
    id("io.gitlab.arturbosch.detekt") version "1.16.0"
    // https://github.com/Kotlin/KEEP/blob/master/proposals/explicit-api-mode.md
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.5.0"
}

allprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    dependencies {
        detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.16.0")
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
