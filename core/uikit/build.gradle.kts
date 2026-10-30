import org.gradle.api.tasks.testing.Test

plugins {
    id("finpulse.kmp.compose")
}

kotlin {
    sourceSets.named("commonMain") {
        dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
        }
    }
    sourceSets.named("jvmMain") {
        dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
    sourceSets.named("jvmTest") {
        dependencies {
            implementation(compose.desktop.currentOs)
            implementation(compose.desktop.uiTestJUnit4)
            implementation(libs.roborazzi.compose.desktop)
        }
    }
}

tasks.withType<Test>().configureEach {
    if (name != "jvmTest") {
        return@configureEach
    }
    val record = providers.gradleProperty("roborazzi.record").orNull == "true"
    systemProperty("roborazzi.test.record", record.toString())
    systemProperty("roborazzi.test.verify", (!record).toString())
}
