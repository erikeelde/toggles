plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.gms.oss.licenses.plugin")
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

android {
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    buildFeatures {
        dataBinding = true
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
        val togglesPermission = "se.eelde.toggles.configprovider.permission" // not used yet

        manifestPlaceholders["wrenchAuthority"] = wrenchAuthority
        manifestPlaceholders["wrenchPermission"] = wrenchPermission
        manifestPlaceholders["togglesAuthority"] = togglesAuthority
        manifestPlaceholders["togglesPermission"] = togglesPermission

        buildConfigField("String", "CONFIG_AUTHORITY", "\"$wrenchAuthority\"")
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
            isMinifyEnabled = true
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
    useLibrary("android.test.runner")
    useLibrary("android.test.base")
    useLibrary("android.test.mock")
}

dependencies {
    testImplementation("junit:junit:4.13")
    testImplementation("org.mockito:mockito-core:3.3.3")

    testImplementation("androidx.test:core:1.2.0")
    testImplementation("androidx.test.ext:truth:1.2.0")
    testImplementation("androidx.test:rules:1.2.0")
    testImplementation("androidx.test:runner:1.2.0")
    testImplementation("androidx.test.ext:junit:1.1.1")
    testImplementation("org.mockito:mockito-android:3.3.3")
    testImplementation("androidx.room:room-testing:2.2.5")
    testImplementation("org.robolectric:robolectric:4.3.1")

    androidTestImplementation("androidx.test:core:1.2.0")
    androidTestImplementation("androidx.test.ext:truth:1.2.0")
    androidTestImplementation("androidx.test:rules:1.2.0")
    androidTestImplementation("androidx.test:runner:1.2.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    androidTestImplementation("org.mockito:mockito-android:3.3.3")
    androidTestImplementation("androidx.room:room-testing:2.2.5")

    implementation("androidx.lifecycle:lifecycle-common-java8:2.2.0")
    // kapt("androidx.lifecycle:lifecycle-compiler:2.2.0")
    kapt("androidx.room:room-compiler:2.2.5")

    implementation("com.google.dagger:dagger:2.28")
    kapt("com.google.dagger:dagger-compiler:2.28")
    implementation("com.google.dagger:dagger-android-support:2.28")
    kapt("com.google.dagger:dagger-android-processor:2.28")

    implementation("androidx.appcompat:appcompat:1.1.0")
    implementation("androidx.recyclerview:recyclerview:1.1.0")
    implementation("com.google.android.material:material:1.3.0-alpha01")
    implementation("androidx.constraintlayout:constraintlayout:2.0.0-beta7")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.2.0")
    implementation("androidx.lifecycle:lifecycle-livedata-core-ktx:2.2.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.2.0")
    implementation("androidx.room:room-runtime:2.2.5")
    implementation("androidx.room:room-ktx:2.2.5")
    implementation("androidx.paging:paging-runtime:2.1.2")

    implementation("androidx.navigation:navigation-fragment-ktx:2.3.0-rc01")
    implementation("androidx.navigation:navigation-ui-ktx:2.3.0-rc01")

    implementation(project(":toggles-core"))
    implementation(project(":toggles-prefs"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.72")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.7")

    implementation("com.google.dagger:dagger:2.28")
    kapt("com.google.dagger:dagger-compiler:2.28")
    implementation("com.google.dagger:dagger-android-support:2.28")
    kapt("com.google.dagger:dagger-android-processor:2.28")
    implementation("androidx.core:core-ktx:1.3.0")
}
