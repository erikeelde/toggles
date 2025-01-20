import com.vanniktech.maven.publish.SonatypeHost
import java.io.FileInputStream
import java.util.Properties

plugins {
    id("toggles.android.library-conventions")
    id("toggles.ownership-conventions")
    id("org.jetbrains.dokka")
    alias(libs.plugins.com.vanniktech.maven.publish)
    alias(libs.plugins.org.jetbrains.kotlinx.binary.compatibility.validator)
}

android {
    namespace = "se.eelde.toggles.flow"
}

dependencies {
    implementation(projects.togglesCore)

    implementation(libs.androidx.annotation)
    implementation(platform(libs.org.jetbrains.kotlinx.kotlinx.coroutines.bom))
    implementation(libs.org.jetbrains.kotlinx.kotlinx.coroutines.android)

    testImplementation(libs.junit)

    testImplementation(libs.androidx.test.core.ktx)
    testImplementation(libs.androidx.test.ext.truth)
    testImplementation(libs.androidx.test.rules)
    testImplementation(libs.androidx.test.runner)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.org.robolectric)
    testImplementation(libs.androidx.test.espresso.espresso.core)
    testImplementation(libs.androidx.arch.core.core.testing)
    testImplementation(libs.org.jetbrains.kotlinx.kotlinx.coroutines.test)
    testImplementation(libs.app.cash.turbine)
    testImplementation(projects.modules.provider)
     testImplementation(testFixtures(projects.modules.provider))
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
    @Suppress("UnstableApiUsage")
    coordinates("se.eelde.toggles", "toggles-flow", version.toString())

    @Suppress("UnstableApiUsage")
    pom {
        name = "Toggles Flow"
        description = "Toggles"
        inceptionYear = "2018"
        url = "https://github.com/erikeelde/toggles"

        licenses {
            license {
                name = "MIT License"
                url = "https://opensource.org/licenses/MIT"
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

    publishToMavenCentral(SonatypeHost.S01)

    signAllPublications()
}