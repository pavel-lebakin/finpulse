plugins {
    id("finpulse.kmp.library")
}

kotlin {
    // The default hierarchy is applied after this script, so androidMain does not exist yet.
    applyDefaultHierarchyTemplate()
    sourceSets.named("commonMain") {
        dependencies {
            api(projects.core.network)
            api(projects.core.security)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
        }
    }
    sourceSets.named("androidMain") {
        dependencies {
            implementation(libs.mlkit.genai.prompt)
        }
    }
    sourceSets.named("jvmTest") {
        dependencies {
            implementation(libs.kotlinx.coroutines.test)
            implementation(projects.core.common)
        }
    }
}
