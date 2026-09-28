plugins {
    id("finpulse.kmp.library")
}

kotlin {
    sourceSets.named("commonMain") {
        dependencies {
            api(libs.navigation3.ui)
        }
    }
}
