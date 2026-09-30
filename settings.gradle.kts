pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "finpulse"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    ":androidApp",
    ":shared",
    ":core:common",
    ":core:mvi",
    ":core:navigation",
    ":core:testing",
    ":core:uikit",
    ":catalog:shared",
    ":catalog:android",
    ":catalog:desktop",
    ":tooling:architecture-tests",
)

// Renaming changes the Gradle path to `:catalog:catalogShared`, so it does not clash with `:shared`.
project(":catalog:shared").name = "catalogShared"
