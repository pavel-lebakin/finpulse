plugins {
    id("finpulse.kmp.library")
}

kotlin {
    sourceSets.named("commonMain") {
        dependencies {
            implementation(projects.core.common)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.lifecycle.viewmodel)
        }
    }
    sourceSets.named("jvmTest") {
        dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
