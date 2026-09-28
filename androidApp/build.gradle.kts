plugins {
    id("finpulse.android.application")
}

android {
    namespace = "com.lebakin.finpulse"
    defaultConfig {
        applicationId = "com.lebakin.finpulse"
        versionCode = 1
        versionName = "0.1.0"
    }
}

dependencies {
    implementation(projects.shared)
    implementation(libs.androidx.activity.compose)
}
