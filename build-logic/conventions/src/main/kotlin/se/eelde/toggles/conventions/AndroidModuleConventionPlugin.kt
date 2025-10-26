package se.eelde.toggles.conventions

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import se.eelde.toggles.conventions.configurations.configureGradleManagedDevices
import se.eelde.toggles.conventions.configurations.configureKotlinAndroid
import se.eelde.toggles.conventions.configurations.libs

class AndroidModuleConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "com.android.library")
            apply(plugin = "org.jetbrains.kotlin.android")
            apply(plugin = "toggles.android.lint")
            apply(plugin = "toggles.detekt.common")

            extensions.configure<LibraryExtension> {
                @Suppress("UnstableApiUsage")
                testFixtures.enable = true
                configureKotlinAndroid(this)
                testOptions.animationsDisabled = true
                configureGradleManagedDevices(this)

                dependencies {
                    "testImplementation"(libs.findLibrary("androidx-test-runner").get())
                }
            }

            tasks.withType(Test::class.java) {
                failOnNoDiscoveredTests.set(false)
            }
        }
    }
}