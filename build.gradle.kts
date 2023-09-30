import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import java.util.Locale

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath(libs.com.android.tools.build.gradle)
        classpath(libs.org.jetbrains.kotlin.kotlin.gradle.plugin)
        classpath(libs.com.google.dagger.hilt.android.gradle.plugin)
        classpath(libs.com.google.gms.google.services)
        classpath(libs.org.jetbrains.dokka.dokka.gradle.plugin)
        classpath(libs.com.google.firebase.firebase.crashlytics.gradle)
        classpath(libs.app.cash.licensee.licensee.gradle.plugin)
    }
}

plugins {
    alias(libs.plugins.com.github.ben.manes.versions)
    alias(libs.plugins.nl.littlerobots.version.catalog.update)
    // https://github.com/Kotlin/KEEP/blob/master/proposals/explicit-api-mode.md
    alias(libs.plugins.com.github.triplet.play) apply (false)
    id("toggles.ownership-conventions")
    id("com.google.devtools.ksp") version "1.9.10-1.0.13" apply false
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any {
        version.uppercase(Locale.getDefault()).contains(it)
    }
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
