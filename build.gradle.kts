import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {

    val roomVersion by extra("2.4.0-alpha01")
    val composeVersion by extra("1.0.0-beta05")
    val lifecycleVersion by extra("2.3.1")

    repositories {
        google()
        mavenCentral()
        jcenter()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.0.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.32")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.3.5")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.38.1")
        classpath("com.google.gms:google-services:4.3.5")
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.14.2")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.5.0")
        classpath("com.google.android.gms:oss-licenses-plugin:0.10.4")
    }
}

plugins {
    id("com.github.ben-manes.versions") version "0.38.0"
    // id("se.eelde.build-optimizations") version "0.2.0"
    id("io.gitlab.arturbosch.detekt") version "1.16.0"
    // https://github.com/Kotlin/KEEP/blob/master/proposals/explicit-api-mode.md
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.7.1"
    id("com.github.triplet.play") version "3.5.0-agp7.0" apply false

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
        maven("https://s01.oss.sonatype.org/service/local/repositories/seeelde-1001/content")
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
