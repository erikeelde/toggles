package se.eelde.toggles.conventions

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import se.eelde.toggles.conventions.configurations.configureKotlinAndroid
import se.eelde.toggles.conventions.configurations.libs
import tapmoc.configureJavaCompatibility
import tapmoc.configureKotlinCompatibility

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "com.android.library")
            apply(plugin = "toggles.android.lint")
            apply(plugin = "toggles.detekt.library")
            apply(plugin = "toggles.dagp")
            apply(plugin = "org.jetbrains.dokka")
            apply(plugin = "com.vanniktech.maven.publish")
            apply(plugin = "org.jetbrains.kotlinx.binary-compatibility-validator")

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(commonExtension = this, enableExplicitApi = false)

                publishing {
                    multipleVariants {
                        allVariants()
                        withSourcesJar()
                        withJavadocJar()
                    }
                }
            }

            configureJavaCompatibility(8)
            configureKotlinCompatibility(libs.findVersion("kotlinCompatibility").get().requiredVersion)

            tasks.withType(Test::class.java) {
                failOnNoDiscoveredTests.set(false)
            }
        }
    }
}