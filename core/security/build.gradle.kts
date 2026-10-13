plugins {
    id("finpulse.kmp.library")
}

kotlin {
    sourceSets.named("commonMain") {
        dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.androidx.datastore.preferences.core)
            implementation(libs.okio)
        }
    }
    sourceSets.named("jvmTest") {
        dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
