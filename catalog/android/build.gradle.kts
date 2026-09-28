plugins {
    id("finpulse.android.application")
}

android {
    namespace = "com.lebakin.finpulse.catalog"
    defaultConfig {
        applicationId = "com.lebakin.finpulse.catalog"
        versionCode = 1
        versionName = "0.1.0"
    }
}

dependencies {
    implementation(projects.catalog.catalogShared)
    implementation(libs.androidx.activity.compose)
}
