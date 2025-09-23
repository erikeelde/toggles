package se.eelde.toggles.conventions.configurations

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

internal fun Project.configureDetekt(libraryRules: Boolean) {
    with(pluginManager) {
        apply(DetektPlugin::class.java)
    }

    dependencies {
        add(
            "detektPlugins",
            libs.findLibrary("io.gitlab.arturbosch.detekt.detekt.formatting").get()
        )
        if (libraryRules) {
            add(
                "detektPlugins",
                libs.findLibrary("io.gitlab.arturbosch.detekt.detekt.rules.libraries").get()
            )
        }
    }

    extensions.configure<DetektExtension> {
        autoCorrect = true
        ignoreFailures = false
        buildUponDefaultConfig = true
        config.setFrom(files(rootProject.file("config/detekt/detekt.yml")))
        baseline = file(project.file("detekt-baseline.xml"))
        parallel = true
        basePath = projectDir.absolutePath
    }

    tasks.withType<Detekt>().configureEach {
        basePath = projectDir.canonicalPath
        reports {
            html.required.set(true)
        }
        exclude {
            it.file.absolutePath.contains("build/")
        }
    }
}