plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "app.morphe.extension"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
