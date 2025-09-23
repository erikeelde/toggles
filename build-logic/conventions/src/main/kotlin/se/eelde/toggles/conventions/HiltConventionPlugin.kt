package se.eelde.toggles.conventions

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import se.eelde.toggles.conventions.configurations.libs

class HiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "com.google.devtools.ksp")

            dependencies {
                "ksp"(libs.findLibrary("com.google.dagger.hilt.compiler").get())
            }

            // Add support for Jvm Module, base on org.jetbrains.kotlin.jvm
            pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
                dependencies {
                    "implementation"(libs.findLibrary("com.google.dagger.hilt.core").get())
                }
            }

            /** Add support for Android modules, based on [AndroidBasePlugin] */
            pluginManager.withPlugin("com.android.base") {
                apply(plugin = "dagger.hilt.android.plugin")
                dependencies {
                    "implementation"(libs.findLibrary("com.google.dagger.hilt.android").get())
                }
            }
        }
    }
}