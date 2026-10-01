package com.lebakin.finpulse.gradle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ModuleGraphRulesTest {
    @Test
    fun currentShapeWithoutFeaturesIsClean() {
        val edges = listOf(
            edge(":core:mvi", ":core:common"),
            edge(":core:testing", ":core:navigation"),
            edge(":shared", ":core:common"),
            edge(":shared", ":core:navigation"),
            edge(":shared", ":core:uikit"),
            edge(":catalog:catalogShared", ":core:uikit"),
            edge(":catalog:android", ":catalog:catalogShared"),
            edge(":catalog:desktop", ":catalog:catalogShared"),
            edge(":androidApp", ":shared"),
            external(":core:navigation", "org.jetbrains.androidx.navigation3", "navigation3-ui"),
        )
        assertEquals(emptyList(), ModuleGraphRules.violations(edges, paths))
    }

    @Test
    fun featureImplMustNotDependOnAnotherImpl() {
        val edges = clean() + edge(":feature:ledger:impl", ":feature:insights:impl")
        assertEquals(
            listOf(":feature:ledger:impl must not depend on feature impl :feature:insights:impl"),
            ModuleGraphRules.violations(edges, paths),
        )
    }

    @Test
    fun onlySharedMayDependOnFeatureImpl() {
        val edges = clean() + edge(":androidApp", ":feature:ledger:impl")
        assertEquals(
            listOf("only :shared may depend on feature impl :feature:ledger:impl (found :androidApp)"),
            ModuleGraphRules.violations(edges, paths),
        )
    }

    @Test
    fun sharedMayDependOnFeatureImplButMustNotExportIt() {
        val direct = clean() + edge(":shared", ":feature:ledger:impl", configuration = "commonMainImplementation")
        assertEquals(emptyList(), ModuleGraphRules.violations(direct, paths))

        val exported = clean() + edge(":shared", ":feature:ledger:impl", configuration = "commonMainApi")
        assertEquals(
            listOf(":shared must not export feature impl :feature:ledger:impl through commonMainApi"),
            ModuleGraphRules.violations(exported, paths),
        )
    }

    @Test
    fun featureApiAllowsOnlyCoreCommonAndKotlinx() {
        val allowed = clean() + listOf(
            edge(":feature:ledger:api", ":core:common"),
            external(":feature:ledger:api", "org.jetbrains.kotlinx", "kotlinx-coroutines-core"),
            external(":feature:ledger:api", "org.jetbrains.kotlin", "kotlin-stdlib"),
        )
        assertEquals(emptyList(), ModuleGraphRules.violations(allowed, paths))

        val projectDep = clean() + edge(":feature:ledger:api", ":feature:insights:api")
        val externalDep = clean() + external(
            ":feature:ledger:api",
            "org.jetbrains.compose.runtime",
            "runtime",
        )
        assertEquals(
            listOf(":feature:ledger:api must depend only on :core:common and kotlinx (found :feature:insights:api)"),
            ModuleGraphRules.violations(projectDep, paths),
        )
        assertEquals(
            listOf(
                ":feature:ledger:api must depend only on :core:common and kotlinx " +
                    "(found org.jetbrains.compose.runtime:runtime on commonMainImplementation)",
            ),
            ModuleGraphRules.violations(externalDep, paths),
        )
    }

    @Test
    fun coreAndCatalogMustNotDependOnFeatures() {
        val edges = clean() + listOf(
            edge(":core:mvi", ":feature:ledger:api"),
            edge(":catalog:android", ":feature:onboarding:api"),
        )
        assertEquals(
            listOf(
                ":catalog:android must not depend on feature :feature:onboarding:api",
                ":core:mvi must not depend on feature :feature:ledger:api",
            ),
            ModuleGraphRules.violations(edges, paths),
        )
    }

    @Test
    fun catalogSharedMustDependOnUikit() {
        val edges = clean().filterNot { it.links(":catalog:catalogShared", ":core:uikit") }
        assertEquals(
            listOf(":catalog:catalogShared must depend on :core:uikit"),
            ModuleGraphRules.violations(edges, paths),
        )
    }

    @Test
    fun productionConfigurationsSkipTests() {
        assertTrue(ModuleGraphRules.isProductionConfiguration("commonMainImplementation"))
        assertTrue(ModuleGraphRules.isProductionConfiguration("commonMainApi"))
        assertTrue(ModuleGraphRules.isProductionConfiguration("implementation"))
        assertFalse(ModuleGraphRules.isProductionConfiguration("commonTestImplementation"))
        assertFalse(ModuleGraphRules.isProductionConfiguration("testImplementation"))
        assertFalse(ModuleGraphRules.isProductionConfiguration("kotlinCompilerPluginClasspath"))
    }

    private val paths = setOf(
        ":androidApp",
        ":shared",
        ":core:common",
        ":core:mvi",
        ":core:navigation",
        ":core:testing",
        ":core:uikit",
        ":catalog:catalogShared",
        ":catalog:android",
        ":catalog:desktop",
    )

    private fun clean(): List<ModuleEdge> = listOf(
        edge(":catalog:catalogShared", ":core:uikit"),
    )

    private fun edge(
        from: String,
        toProject: String,
        configuration: String = "commonMainImplementation",
    ) = ModuleEdge(from, configuration, toProject, null, null)

    private fun external(
        from: String,
        group: String,
        module: String,
    ) = ModuleEdge(from, "commonMainImplementation", null, group, module)
}
