plugins {
    `kotlin-dsl`
    alias(libs.plugins.com.android.lint)
}

group = "se.eelde.toggles.conventions"

kotlin {
    jvmToolchain(21)
}

dependencies {
    // implementation("gradle.plugin.com.github.spotbugs.snom:spotbugs-gradle-plugin:4.7.2")
    compileOnly(libs.com.android.tools.build.gradle)
    compileOnly(libs.android.tools.common)
    compileOnly(libs.org.jetbrains.kotlin.compose.compiler.gradle.plugin)
    compileOnly(libs.org.jetbrains.kotlin.kotlin.gradle.plugin)
    compileOnly(libs.com.google.devtools.ksp.com.google.devtools.ksp.gradle.plugin)
    compileOnly(libs.io.gitlab.arturbosch.detekt.detekt.gradle.plugin)
    compileOnly(libs.com.vanniktech.gradle.maven.publish.plugin)
    compileOnly(libs.org.jetbrains.kotlinx.binary.compatibility.validator)
    compileOnly(libs.org.jetbrains.dokka.dokka.gradle.plugin)
    lintChecks(libs.androidx.lint.gradle)
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = libs.plugins.toggles.android.application.get().pluginId
            implementationClass = "se.eelde.toggles.conventions.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = libs.plugins.toggles.android.library.get().pluginId
            implementationClass = "se.eelde.toggles.conventions.AndroidLibraryConventionPlugin"
        }

        register("androidModule") {
            id = libs.plugins.toggles.android.module.get().pluginId
            implementationClass = "se.eelde.toggles.conventions.AndroidModuleConventionPlugin"
        }
        register("androidLint") {
            id = libs.plugins.toggles.android.lint.get().pluginId
            implementationClass = "se.eelde.toggles.conventions.AndroidLintConventionPlugin"
        }
        register("detektCommon") {
            id = libs.plugins.toggles.detekt.common.get().pluginId
            implementationClass = "se.eelde.toggles.conventions.DetektConventionPlugin"
        }
        register("detektLibrary") {
            id = libs.plugins.toggles.detekt.library.get().pluginId
            implementationClass = "se.eelde.toggles.conventions.DetektLibraryConventionPlugin"
        }
        register("hilt") {
            id = libs.plugins.toggles.hilt.get().pluginId
            implementationClass = "se.eelde.toggles.conventions.HiltConventionPlugin"
        }
        register("androidApplicationCompose") {
            id = libs.plugins.toggles.android.compose.application.get().pluginId
            implementationClass =
                "se.eelde.toggles.conventions.AndroidApplicationComposeConventionPlugin"
        }
        register("androidModuleCompose") {
            id = libs.plugins.toggles.android.compose.module.get().pluginId
            implementationClass =
                "se.eelde.toggles.conventions.AndroidModuleComposeConventionPlugin"
        }
    }
}