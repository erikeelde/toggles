// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
        jcenter()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:4.1.0-rc03")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.10")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.3.0")
        classpath("com.google.gms:oss-licenses:0.9.2")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.28-alpha")
    }
}

plugins {
    id("com.github.ben-manes.versions") version "0.33.0"
    id("se.eelde.build-optimizations") version "0.1.2"
    id("com.github.plnice.canidropjetifier") version "0.5"
    id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
}

allprojects {
    repositories {
        google()
        mavenCentral()
        jcenter()
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}
