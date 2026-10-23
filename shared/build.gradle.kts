import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    id("finpulse.kmp.compose")
    alias(libs.plugins.aboutlibraries)
}

val licenseNoticesDir = layout.buildDirectory.dir("generated/aboutLibraries")

aboutLibraries {
    offlineMode.set(true)
    collect {
        fetchRemoteLicense.set(false)
        fetchRemoteFunding.set(false)
        includePlatform.set(true)
        includeTargets.set(true)
    }
    license {
        strictMode.set(com.mikepenz.aboutlibraries.plugin.StrictMode.WARN)
    }
    export {
        prettyPrint.set(true)
        excludeFields.set(setOf("generated"))
        outputFile.set(licenseNoticesDir.map { it.file("files/aboutlibraries.json") })
    }
}

configure<org.jetbrains.compose.ComposeExtension> {
    extensions.configure<org.jetbrains.compose.resources.ResourcesExtension>("resources") {
        packageOfResClass = "com.lebakin.finpulse.generated.resources"
        customDirectory("commonMain", licenseNoticesDir)
    }
}

tasks.withType<com.mikepenz.aboutlibraries.plugin.AboutLibrariesTask>().configureEach {
    enabled = name == "exportLibraryDefinitions"
}

tasks.configureEach {
    if (name == "copyNonXmlValueResourcesForCommonMain" || name == "convertXmlValueResourcesForCommonMain") {
        dependsOn("exportLibraryDefinitions")
    }
}

kotlin {
    targets.withType<KotlinNativeTarget>().configureEach {
        binaries.framework {
            baseName = "Shared"
            isStatic = true
            export(projects.core.ai)
        }
    }
    sourceSets.named("commonMain") {
        dependencies {
            implementation(projects.core.common)
            implementation(projects.core.database)
            implementation(projects.core.navigation)
            implementation(projects.core.preferences)
            implementation(projects.core.security)
            implementation(projects.core.network)
            api(projects.core.ai)
            implementation(projects.core.uikit)
            implementation(projects.feature.ledger.impl)
            implementation(projects.feature.onboarding.impl)
            implementation(projects.feature.insights.impl)
            implementation(projects.feature.scanner.impl)
            implementation(projects.feature.assistant.impl)
            implementation(projects.feature.settings.impl)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.navigation3.ui)
            implementation(compose.components.resources)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
        }
    }
    sourceSets.named("commonTest") {
        dependencies {
            implementation(libs.koin.test)
            implementation(libs.kotlinx.coroutines.test)
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
