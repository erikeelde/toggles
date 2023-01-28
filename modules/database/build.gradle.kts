plugins {
    id("toggles.android.module-conventions")
    id("toggles.ownership-conventions")
}

class RoomSchemaArgProvider(
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val schemaDir: File
) : CommandLineArgumentProvider {

    override fun asArguments(): Iterable<String> {
        // Note: If you're using KSP, you should change the line below to return
        // listOf("room.schemaLocation=${schemaDir.path}")
        return listOf("-Aroom.schemaLocation=${schemaDir.path}")
    }
}

android {
    namespace = "se.eelde.toggles.database"
    defaultConfig {
        javaCompileOptions {
            annotationProcessorOptions {
                compilerArgumentProviders(
                    RoomSchemaArgProvider(File(projectDir, "schemas"))
                )
                arguments["room.incremental"] = "true"
                arguments["room.expandProjection"] = "true"
            }
        }
    }
    sourceSets {
        // Adds exported schema location as test app assets.
        getByName("androidTest").assets.srcDir("$projectDir/schemas")
    }
//    sourceSets {
//        // debug.assets.srcDirs => https://github.com/robolectric/robolectric/issues/3928
//        // debug.assets.srcDirs += files("$projectDir/schemas".toString())
//        getByName("test") {
//            assets.srcDirs(files("$projectDir/schemas"))
//        }
//    }
}

dependencies {
    implementation(libs.androidx.room.room.paging)
    implementation(libs.androidx.room.room.runtime)
    implementation(libs.androidx.room.room.ktx)
    kapt(libs.androidx.room.room.compiler)
    implementation(libs.se.eelde.toggles.toggles.core)
    implementation(libs.androidx.core.core.ktx)
    implementation(libs.androidx.appcompat)

    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.org.robolectric)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.room.room.testing)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.espresso.core)
}
