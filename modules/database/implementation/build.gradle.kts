plugins {
    alias(libs.plugins.toggles.android.module)
    alias(libs.plugins.com.google.devtools.ksp)
}

class RoomSchemaArgProvider(
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val schemaDir: File
) : CommandLineArgumentProvider {
    override fun asArguments(): Iterable<String> {
        return listOf("room.schemaLocation=${schemaDir.path}", "room.generateKotlin=true")
    }
}

android {
    namespace = "se.eelde.toggles.database.implementation"
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
    sourceSets {
        getByName("test").assets.srcDir("$projectDir/schemas")
    }
}

ksp {
    arg(RoomSchemaArgProvider(File(projectDir, "schemas")))
}

dependencies {
    implementation(libs.androidx.room.room.runtime)
    ksp(libs.androidx.room.room.compiler)
    implementation(projects.togglesCore)

    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.org.robolectric)
    testImplementation(libs.junit)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.androidx.room.room.testing)

    testFixturesImplementation(libs.androidx.room.room.runtime)
    api(libs.org.jetbrains.kotlinx.kotlinx.coroutines.core)
    implementation(libs.androidx.room.room.common)
    testImplementation(libs.androidx.test.monitor)
    implementation(libs.androidx.collection)
    testImplementation(libs.androidx.sqlite.sqlite.framework)
    implementation(libs.androidx.sqlite)
    testImplementation(libs.androidx.sqlite)
}
