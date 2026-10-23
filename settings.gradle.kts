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
    ":core:database",
    ":core:preferences",
    ":core:security",
    ":core:network",
    ":core:ai",
    ":feature:ledger:api",
    ":feature:ledger:impl",
    ":feature:onboarding:api",
    ":feature:onboarding:impl",
    ":feature:insights:api",
    ":feature:insights:impl",
    ":feature:scanner:api",
    ":feature:scanner:impl",
    ":feature:assistant:api",
    ":feature:assistant:impl",
    ":feature:settings:api",
    ":feature:settings:impl",
    ":catalog:shared",
    ":catalog:android",
    ":catalog:desktop",
    ":tooling:architecture-tests",
)

// Renaming changes the Gradle path to `:catalog:catalogShared`, so it does not clash with `:shared`.
project(":catalog:shared").name = "catalogShared"
