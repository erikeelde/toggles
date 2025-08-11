import org.gradle.accessors.dm.LibrariesForLibs

val libs = the<LibrariesForLibs>()

plugins {
    id("io.gitlab.arturbosch.detekt")
}

dependencies {
    detektPlugins(libs.io.gitlab.arturbosch.detekt.detekt.formatting)
}

detekt {
    autoCorrect = true
    buildUponDefaultConfig = true
    baseline = project.file("config/detekt/baseline.xml")
//    config = rootProject.files("config/detekt/detekt.yml")
}
