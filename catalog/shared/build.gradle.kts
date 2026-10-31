plugins {
    id("finpulse.kmp.compose")
}

kotlin {
    sourceSets.named("commonMain") {
        dependencies {
            implementation(projects.core.uikit)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
        }
    }
    sourceSets.named("androidMain") {
        dependencies {
            implementation(libs.androidx.activity.compose)
        }
    }
    sourceSets.named("jvmMain") {
        dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}
