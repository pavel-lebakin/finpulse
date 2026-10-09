plugins {
    id("finpulse.kmp.library")
}

kotlin {
    sourceSets.named("commonMain") {
        dependencies {
            api(projects.feature.ledger.api)
            implementation(projects.core.database)
            implementation(projects.core.preferences)
            implementation(libs.koin.core)
        }
    }
    sourceSets.named("commonTest") {
        dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
    }
    sourceSets.named("jvmTest") {
        dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
