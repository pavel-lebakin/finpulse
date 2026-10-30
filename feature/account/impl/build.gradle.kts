plugins {
    id("finpulse.kmp.library")
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

kotlin {
    sourceSets.named("commonMain") {
        dependencies {
            api(projects.feature.account.api)
            implementation(projects.core.database)
            implementation(libs.koin.core)
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
    sourceSets.named("commonTest") {
        dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

dependencies {
    listOf(
        "kspAndroid",
        "kspIosArm64",
        "kspIosSimulatorArm64",
        "kspJvm",
    ).forEach { configuration ->
        add(configuration, libs.androidx.room.compiler)
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}
