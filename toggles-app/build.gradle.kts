plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.gms.oss.licenses.plugin")
    id("dagger.hilt.android.plugin")
}

// https://github.com/gradle/kotlin-dsl/issues/644#issuecomment-398502551
androidExtensions { isExperimental = true }

kapt {
    javacOptions {
        // Increase the max count of errors from annotation processors.
        // Default is 100.
        option("-Xmaxerrs", 500)
    }
}

hilt {
    enableTransformForLocalTests = true
}

android {
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    compileSdkVersion(Versions.compileSdk)
    defaultConfig {
        applicationId = "se.eelde.toggles"
        minSdkVersion(Versions.minSdk)
        targetSdkVersion(Versions.targetSdk)
        versionCode = Versions.appVersionCode
        versionName = Versions.appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
        }

        val wrenchAuthority = "com.izettle.wrench.configprovider"
        val wrenchPermission = "com.izettle.wrench.permission"
        val togglesAuthority = "se.eelde.toggles.configprovider" // not used yet
        val togglesPermission = "se.eelde.toggles.provider_permission" // not used yet

        manifestPlaceholders["wrenchAuthority"] = wrenchAuthority
        manifestPlaceholders["wrenchPermission"] = wrenchPermission
        manifestPlaceholders["togglesAuthority"] = togglesAuthority
        manifestPlaceholders["togglesPermission"] = togglesPermission

        buildConfigField("String", "CONFIG_AUTHORITY", "\"$togglesAuthority\"")
    }
    packagingOptions {
        exclude("META-INF/main.kotlin_module")
        exclude("mockito-extensions/org.mockito.plugins.MockMaker")
        exclude("META-INF/atomicfu.kotlin_module")
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"))
        }
        getByName("debug") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"))

            versionNameSuffix = " debug"
            applicationIdSuffix = ".debug"
        }
    }
    lintOptions {
        baselineFile = file("lint-baseline.xml")
        isCheckReleaseBuilds = true
        isAbortOnError = true
        isWarningsAsErrors = true
        lintConfig = File("../lint.xml")
    }
    sourceSets {
        // debug.assets.srcDirs => https://github.com/robolectric/robolectric/issues/3928
        // debug.assets.srcDirs += files("$projectDir/schemas".toString())
        getByName("androidTest") {
            assets.srcDirs(files("$projectDir/schemas"))
        }
    }
}

dependencies {
    testImplementation("junit:junit:4.13")
    testImplementation("org.mockito:mockito-core:3.5.13")

    testImplementation("androidx.test:core:1.3.0")
    testImplementation("androidx.test.ext:truth:1.3.0")
    testImplementation("androidx.test:rules:1.3.0")
    testImplementation("androidx.test:runner:1.3.0")
    testImplementation("androidx.test.ext:junit:1.1.2")
    testImplementation("org.mockito:mockito-android:3.5.13")
//    testImplementation("androidx.room:room-testing:2.3.0-alpha02")
    testImplementation("org.robolectric:robolectric:4.4")
    testImplementation("androidx.test.espresso:espresso-core:3.3.0")
    testImplementation("androidx.arch.core:core-testing:2.1.0")
    testImplementation("androidx.lifecycle:lifecycle-runtime:2.3.0-alpha07")
    implementation("androidx.lifecycle:lifecycle-runtime:2.3.0-alpha07")
    androidTestImplementation("androidx.lifecycle:lifecycle-runtime:2.3.0-alpha07")

    androidTestImplementation("androidx.test:core:1.3.0")
    androidTestImplementation("androidx.test.ext:truth:1.3.0")
    androidTestImplementation("androidx.test:rules:1.3.0")
    androidTestImplementation("androidx.test:runner:1.3.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("org.mockito:mockito-android:3.5.13")
    androidTestImplementation("androidx.room:room-testing:2.3.0-alpha02")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")

    implementation("com.google.dagger:hilt-android:2.28-alpha")
    kapt("com.google.dagger:hilt-android-compiler:2.28-alpha")
    implementation("androidx.hilt:hilt-lifecycle-viewmodel:1.0.0-alpha02")
    kapt("androidx.hilt:hilt-compiler:1.0.0-alpha02")

    testImplementation("com.google.dagger:hilt-android-testing:2.28-alpha")
    kaptTest("com.google.dagger:hilt-android-compiler:2.28-alpha")

    implementation("androidx.lifecycle:lifecycle-common-java8:2.3.0-alpha07")
    kapt("androidx.room:room-compiler:2.3.0-alpha02")

    implementation("com.google.dagger:dagger:2.29.1")
    kapt("com.google.dagger:dagger-compiler:2.29.1")

    implementation("androidx.appcompat:appcompat:1.3.0-alpha02")
    implementation("androidx.recyclerview:recyclerview:1.2.0-alpha05")
    implementation("com.google.android.material:material:1.3.0-alpha02")
    implementation("androidx.constraintlayout:constraintlayout:2.0.1")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.0-alpha07")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.0-alpha07")
    implementation("androidx.lifecycle:lifecycle-livedata-core-ktx:2.3.0-alpha07")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.3.0-alpha07")
    implementation("androidx.room:room-runtime:2.3.0-alpha02")
    implementation("androidx.room:room-ktx:2.3.0-alpha02")
    implementation("androidx.paging:paging-runtime:3.0.0-alpha06")

    implementation("androidx.navigation:navigation-fragment-ktx:2.3.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.3.0")

    implementation(project(":toggles-core"))
    implementation(project(":toggles-prefs"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.4.10")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9")

    implementation("androidx.core:core-ktx:1.5.0-alpha03")
}
