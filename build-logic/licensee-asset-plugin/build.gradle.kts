plugins {
    `kotlin-dsl`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
        vendor.set(JvmVendorSpec.AZUL)
    }
}
buildscript {
    dependencies {
        classpath(libs.com.android.tools.build.gradle)
    }
}

dependencies {
    // implementation("gradle.plugin.com.github.spotbugs.snom:spotbugs-gradle-plugin:4.7.2")
    implementation("se.premex:ownership-gradle-plugin:0.0.7")
    implementation(libs.io.gitlab.arturbosch.detekt.detekt.gradle.plugin)
    implementation(libs.com.android.tools.build.gradle)
    implementation(libs.org.jetbrains.kotlin.kotlin.gradle.plugin)

    // https://github.com/gradle/gradle/issues/15383
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

gradlePlugin {
    plugins {
        create("licenseeassetplugin") {
            id = "se.eelde.toggles.licenseeassetplugin"
            group = "se.eelde.toggles"
            implementationClass = "se.eelde.toggles.licenseeassetplugin.CopyLicenseeReportPlugin"
        }
    }
}
