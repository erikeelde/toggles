package se.eelde.toggles.conventions

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import se.eelde.toggles.conventions.configurations.configureGradleManagedDevices
import se.eelde.toggles.conventions.configurations.configureKotlinAndroid
import se.eelde.toggles.conventions.configurations.libs

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "com.android.application")
            apply(plugin = "toggles.android.lint")
            apply(plugin = "toggles.detekt.common")
            apply(plugin = "toggles.dagp")

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = 36
                testOptions.animationsDisabled = true
                configureGradleManagedDevices(this)

                dependencies {
                    "testRuntimeOnly"(libs.findLibrary("androidx-test-runner").get())
                    "androidTestRuntimeOnly"(libs.findLibrary("androidx-test-runner").get())
                }
            }

            tasks.withType(Test::class.java) {
                failOnNoDiscoveredTests.set(false)
            }
        }
    }
}