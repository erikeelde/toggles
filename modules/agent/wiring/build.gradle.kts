plugins {
    alias(libs.plugins.toggles.android.module)
    alias(libs.plugins.toggles.hilt)
}

android {
    namespace = "se.eelde.toggles.agent.wiring"
}

dependencies {
    api(projects.modules.agent.implementation)

    // AgentModule provides TogglesDatabase directly, which extends RoomDatabase; the Room
    // runtime is only exposed as `implementation` from database:implementation, so this module
    // needs its own compile-time dependency on it to resolve TogglesDatabase's supertype
    // (mirrors modules/database/wiring, which has the same requirement for the same reason).
    implementation(libs.androidx.room.room.runtime)

    api(libs.com.google.dagger.hilt.android)
    api(libs.com.google.dagger)
    implementation(libs.com.google.dagger.hilt.core)
    ksp(libs.com.google.dagger.hilt.compiler)
}
