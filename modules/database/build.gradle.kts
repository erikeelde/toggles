plugins {
    id("toggles.android.module-conventions")
    id("toggles.ownership-conventions")
    alias(libs.plugins.com.google.devtools.ksp)
}

class RoomSchemaArgProvider(
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val schemaDir: File
) : CommandLineArgumentProvider {

    override fun asArguments(): Iterable<String> {
        return listOf("room.schemaLocation=${schemaDir.path}")
    }
}

android {
    namespace = "se.eelde.toggles.database"
    testOptions {
        unitTests{
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
    implementation(libs.androidx.room.room.ktx)
    ksp(libs.androidx.room.room.compiler)
    implementation(projects.togglesCore)
    implementation(libs.androidx.core.core.ktx)
    implementation(libs.androidx.appcompat)

    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.org.robolectric)
    testImplementation(libs.junit)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.androidx.room.room.testing)
    testImplementation(libs.androidx.test.espresso.espresso.core)
}
