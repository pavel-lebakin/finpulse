plugins {
    id("finpulse.kmp.library")
}

kotlin {
    // The default hierarchy is applied after this script, so iosMain does not exist yet.
    applyDefaultHierarchyTemplate()
    sourceSets.named("commonMain") {
        dependencies {
            implementation(projects.core.common)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.client.core)
        }
    }
    sourceSets.named("androidMain") {
        dependencies {
            implementation(libs.ktor.client.okhttp)
        }
    }
    sourceSets.named("jvmMain") {
        dependencies {
            implementation(libs.ktor.client.okhttp)
        }
    }
    sourceSets.named("iosMain") {
        dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
    sourceSets.named("jvmTest") {
        dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
