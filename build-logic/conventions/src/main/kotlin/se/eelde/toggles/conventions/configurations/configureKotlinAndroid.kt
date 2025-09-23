/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.eelde.toggles.conventions.configurations

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

/**
 * Configure base Kotlin with Android options
 */
internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
    enableExplicitApi: Boolean = false,
) {
    commonExtension.apply {
        compileSdk = 36

        defaultConfig {
            minSdk = 23
        }
    }

    configureKotlin<KotlinAndroidProjectExtension>(enableExplicitApi)
}

/**
 * Configure base Kotlin options
 */
private inline fun <reified T : KotlinBaseExtension> Project.configureKotlin(enableExplicitApi: Boolean) =
    configure<T> {
        // Treat all Kotlin warnings as errors (disabled by default)
        // Override by setting warningsAsErrors=true in your ~/.gradle/gradle.properties
        val warningsAsErrors = providers.gradleProperty("warningsAsErrors").map {
            it.toBoolean()
        }.orElse(false)
        when (this) {
            is KotlinAndroidProjectExtension -> compilerOptions
            is KotlinJvmProjectExtension -> compilerOptions
            else -> TODO("Unsupported project extension $this ${T::class}")
        }.apply {
            if (enableExplicitApi) {
                explicitApi = ExplicitApiMode.Strict
            }
            jvmToolchain(21)
            allWarningsAsErrors = warningsAsErrors
        }
    }