import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.toggles.android.library)
    id("org.jetbrains.dokka")
    alias(libs.plugins.com.vanniktech.maven.publish)
    alias(libs.plugins.org.jetbrains.kotlinx.binary.compatibility.validator)
}

android {
    namespace = "se.eelde.toggles.flow"

    @Suppress("UnstableApiUsage")
    testFixtures.enable = true
}

dependencies {
    api(projects.togglesCore)

    implementation(platform(libs.org.jetbrains.kotlinx.kotlinx.coroutines.bom))
    runtimeOnly(libs.org.jetbrains.kotlinx.kotlinx.coroutines.android)

    testImplementation(libs.junit)

    testRuntimeOnly(libs.androidx.test.runner)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.org.robolectric)
    testImplementation(libs.org.jetbrains.kotlinx.kotlinx.coroutines.test)
    testImplementation(libs.app.cash.turbine)
    testImplementation(projects.modules.provider.implementation)
    testImplementation(projects.modules.database.implementation)
    testImplementation(libs.androidx.room.room.runtime)
    testImplementation(testFixtures(projects.modules.provider.implementation))
    testImplementation(testFixtures(projects.modules.database.implementation))
    testImplementation(testFixtures(projects.togglesFlow))

    testImplementation(platform(libs.org.jetbrains.kotlinx.kotlinx.coroutines.bom))
    api(libs.org.jetbrains.kotlinx.kotlinx.coroutines.core)
    testImplementation(libs.org.jetbrains.kotlinx.kotlinx.coroutines.core)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.org.robolectric.annotations)
    testImplementation(libs.org.robolectric.shadows.framework)
}

val versionFile = File("versions.properties")
val versions = Properties().apply {
    if (versionFile.exists()) {
        FileInputStream(versionFile).use {
            load(it)
        }
    }
}

version = versions.getProperty("V_LIBRARY_VERSION")
    ?: throw GradleException("No version found in versions.properties")


mavenPublishing {
    coordinates("se.eelde.toggles", "toggles-flow", version.toString())

    pom {
        name = "Toggles Flow"
        description = "Toggles"
        inceptionYear = "2018"
        url = "https://github.com/erikeelde/toggles"

        licenses {
            license {
                name = "MIT License"
                url = "https://opensource.org/license/mit/"
                distribution = "repo"
            }
        }

        developers {
            developer {
                id = "erikeelde"
                name = "Erik Eelde"
            }
            developer {
                id = "warting"
                name = "Stefan Wärting"
            }
        }

        scm {
            url = "https://github.com/erikeelde/toggles"
            connection = "scm:git@github.com:erikeelde/toggles.git"
            developerConnection = "scm:git@github.com:erikeelde/toggles.git"
        }
    }

    publishToMavenCentral()

    signAllPublications()
}