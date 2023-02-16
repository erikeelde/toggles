plugins {
    id("io.gitlab.arturbosch.detekt")
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.22.0")
}

detekt {
    autoCorrect = true
    buildUponDefaultConfig = true
    baseline = project.file("config/detekt/baseline.xml")
    config = rootProject.files("config/detekt/detekt.yml")
}
