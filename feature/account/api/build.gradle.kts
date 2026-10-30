plugins {
    id("finpulse.kmp.library")
}

kotlin {
    sourceSets.named("commonMain") {
        dependencies {
            api(projects.core.common)
            api(libs.kotlinx.coroutines.core)
        }
    }
}
