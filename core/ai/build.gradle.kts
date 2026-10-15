plugins {
    id("finpulse.kmp.library")
}

kotlin {
    sourceSets.named("commonMain") {
        dependencies {
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
