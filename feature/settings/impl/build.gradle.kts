plugins {
    id("finpulse.kmp.compose")
}

kotlin {
    sourceSets.named("commonMain") {
        dependencies {
            api(projects.feature.settings.api)
            implementation(projects.feature.onboarding.api)
            implementation(projects.core.mvi)
            implementation(projects.core.navigation)
            implementation(projects.core.uikit)
            implementation(projects.core.preferences)
            implementation(projects.core.security)
            implementation(libs.koin.core)
            implementation(libs.koin.core.viewmodel)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.lifecycle.viewmodel)
            implementation(libs.lifecycle.runtime.compose)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.aboutlibraries.core)
            implementation(libs.aboutlibraries.compose.m3)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
        }
    }
    sourceSets.named("commonTest") {
        dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
