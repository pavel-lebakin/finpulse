plugins {
    id("finpulse.kmp.compose")
}

kotlin {
    sourceSets.named("commonMain") {
        dependencies {
            api(projects.feature.assistant.api)
            implementation(projects.feature.ledger.api)
            implementation(projects.feature.insights.api)
            implementation(projects.core.ai)
            implementation(projects.core.mvi)
            implementation(projects.core.navigation)
            implementation(projects.core.uikit)
            implementation(libs.koin.core)
            implementation(libs.koin.core.viewmodel)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.kotlinx.datetime)
            implementation(libs.lifecycle.viewmodel)
            implementation(libs.lifecycle.runtime.compose)
            implementation(libs.kotlinx.coroutines.core)
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
