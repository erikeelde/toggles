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
        classpath(libs.app.cash.licensee.licensee.gradle.plugin)
    }
}

plugins {
    alias(libs.plugins.nl.littlerobots.version.catalog.update)
    // https://github.com/Kotlin/KEEP/blob/master/proposals/explicit-api-mode.md
    alias(libs.plugins.com.github.triplet.play) apply (false)
    id("toggles.ownership-conventions")
    alias(libs.plugins.com.google.devtools.ksp) apply false
    alias(libs.plugins.com.autonomousapps.dependency.analysis)
}

develocity {
    buildScan {
        termsOfUseUrl.set("https://gradle.com/help/legal-terms-of-use")
        termsOfUseAgree.set("yes")
    }
}

task<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
