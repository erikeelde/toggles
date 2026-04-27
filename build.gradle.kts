// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath(libs.com.android.tools.build.gradle)
        classpath(libs.com.google.dagger.hilt.android.gradle.plugin)
        classpath(libs.com.google.gms.google.services)
        classpath(libs.org.jetbrains.dokka.dokka.gradle.plugin)
        classpath(libs.app.cash.licensee.licensee.gradle.plugin)
    }
}

plugins {
    alias(libs.plugins.nl.littlerobots.version.catalog.update)
    alias(libs.plugins.com.github.triplet.play) apply false
    alias(libs.plugins.com.google.devtools.ksp) apply false
    alias(libs.plugins.com.autonomousapps.dependency.analysis)

    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.com.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.io.gitlab.arturbosch.detekt) apply false
    alias(libs.plugins.org.jetbrains.kotlinx.binary.compatibility.validator) apply false
    alias(libs.plugins.com.vanniktech.maven.publish) apply false
    alias(libs.plugins.org.jetbrains.dokka) apply false
    alias(libs.plugins.com.google.dagger.hilt.android) apply false
}

develocity {
    buildScan {
        termsOfUseUrl.set("https://gradle.com/help/legal-terms-of-use")
        termsOfUseAgree.set("yes")
    }
}

dependencyAnalysis {
    issues {
        // Robolectric is referenced from test source via @Config and Shadows.shadowOf
        // (compile-time use), so DAGP's testImplementation -> testRuntimeOnly
        // demotion would break compilation.
        project(":modules:database:implementation") {
            onIncorrectConfiguration {
                exclude("org.robolectric:robolectric")
            }
            onRuntimeOnly {
                exclude("org.robolectric:robolectric")
            }
        }
        project(":toggles-flow") {
            onIncorrectConfiguration {
                exclude("org.robolectric:robolectric")
            }
            onRuntimeOnly {
                exclude("org.robolectric:robolectric")
            }
        }

        // hilt-android is applied by the toggles.hilt convention plugin and is
        // required by the dagger.hilt.android Gradle plugin even when DAGP can't
        // see direct usage in the wiring modules (they only use @Module / @InstallIn
        // from hilt-core but the Hilt Android plugin still needs hilt-android present).
        project(":modules:coroutines:wiring") {
            onUnusedDependencies {
                exclude("com.google.dagger:hilt-android")
            }
        }
        // The wiring modules expose Hilt @Provides on their public API surface, so
        // DAGP suggests api. The convention plugin uses implementation since most
        // consumers (including the apps) only need it for runtime DI wiring.
        project(":modules:database:wiring") {
            onIncorrectConfiguration {
                exclude("com.google.dagger:hilt-android")
            }
        }
        project(":modules:provider:wiring") {
            onIncorrectConfiguration {
                exclude("com.google.dagger:hilt-android")
            }
        }
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
