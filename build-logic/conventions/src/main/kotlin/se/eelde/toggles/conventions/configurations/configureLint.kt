package se.eelde.toggles.conventions.configurations

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Configure base Kotlin with Android options
 */
internal fun Project.configureLint(commonExtension: CommonExtension<*, *, *, *, *, *>) {
    dependencies {
        "lintChecks"(libs.findLibrary("com.slack.lint.slack.lint.checks").get())
    }

    commonExtension.apply {
        val enableLintBaseline: Boolean =
            project.properties.getOrDefault("enableLintBaseline", "true").toString().toBoolean()
        val lintAbortOnError: Boolean =
            project.properties.getOrDefault("abortOnError", "true").toString().toBoolean()
        lint {
            baseline = if (enableLintBaseline) {
                file("lint-baseline.xml")
            } else {
                null
            }
            abortOnError = lintAbortOnError
            disable.addAll(setOf("AndroidGradlePluginVersion", "NewerVersionAvailable"))
            checkReleaseBuilds = true
            checkGeneratedSources = true
            lintConfig = rootProject.file("config/lint/lint.xml")
            showAll = true // will soon be enable
            htmlReport = true
            checkAllWarnings = true
            warningsAsErrors = true
        }
    }
}