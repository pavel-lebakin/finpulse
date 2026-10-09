plugins {
    id("finpulse.kmp.library")
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

kotlin {
    sourceSets.named("commonMain") {
        dependencies {
            implementation(libs.kotlinx.coroutines.core)
            api(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
        }
    }
    sourceSets.named("jvmTest") {
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
