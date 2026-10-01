package com.lebakin.finpulse.gradle

/**
 * Dependency boundaries from docs/architecture/modules.md.
 *
 * The `core.database` package rule stays in Konsist. This check sees Gradle
 * project paths and declared production dependencies, not source packages.
 *
 * `:catalog:shared` is renamed in settings.gradle.kts, so its Gradle path is
 * `:catalog:catalogShared`.
 */
object ModuleGraphRules {
    const val CORE_COMMON = ":core:common"
    const val CORE_UIKIT = ":core:uikit"
    const val SHARED = ":shared"
    const val CATALOG_SHARED = ":catalog:catalogShared"

    private val featureApi = Regex("^:feature:[^:]+:api$")
    private val featureImpl = Regex("^:feature:[^:]+:impl$")

    fun isProductionConfiguration(name: String): Boolean {
        val lower = name.lowercase()
        if (lower.contains("test")) return false
        return lower.endsWith("implementation") ||
            lower.endsWith("api") ||
            lower.endsWith("compileonly") ||
            lower.endsWith("runtimeonly")
    }

    fun violations(edges: List<ModuleEdge>, projectPaths: Set<String>): List<String> {
        val problems = mutableListOf<String>()
        edges.distinct().forEach { edge ->
            problems += problemsFor(edge)
        }
        if (CATALOG_SHARED in projectPaths && edges.none { it.links(CATALOG_SHARED, CORE_UIKIT) }) {
            problems += "$CATALOG_SHARED must depend on $CORE_UIKIT"
        }
        return problems.distinct().sorted()
    }

    private fun problemsFor(edge: ModuleEdge): List<String> {
        val target = edge.toProject
        val problems = mutableListOf<String>()
        if (target != null && isFeatureImpl(target) && edge.from != SHARED) {
            problems += if (isFeatureImpl(edge.from)) {
                "${edge.from} must not depend on feature impl $target"
            } else {
                "only $SHARED may depend on feature impl $target (found ${edge.from})"
            }
        }
        if (target != null && isFeatureImpl(target) && edge.from == SHARED && edge.configuration.lowercase().endsWith("api")) {
            problems += "$SHARED must not export feature impl $target through ${edge.configuration}"
        }
        if (isFeatureApi(edge.from)) {
            problems += apiProblems(edge)
        }
        if (isCore(edge.from) && target != null && isFeature(target)) {
            problems += "${edge.from} must not depend on feature $target"
        }
        if (edge.from.startsWith(":catalog:") && target != null && isFeature(target)) {
            problems += "${edge.from} must not depend on feature $target"
        }
        return problems
    }

    private fun apiProblems(edge: ModuleEdge): List<String> {
        val target = edge.toProject
        if (target != null) {
            if (target == CORE_COMMON) return emptyList()
            return listOf("${edge.from} must depend only on $CORE_COMMON and kotlinx (found $target)")
        }
        if (isKotlinx(edge.group, edge.module)) return emptyList()
        val notation = listOfNotNull(edge.group, edge.module).joinToString(":")
        if (notation.isEmpty()) return emptyList()
        return listOf(
            "${edge.from} must depend only on $CORE_COMMON and kotlinx (found $notation on ${edge.configuration})",
        )
    }

    private fun isKotlinx(group: String?, module: String?): Boolean {
        if (group == "org.jetbrains.kotlinx") return true
        // The Kotlin plugin adds stdlib on its own. Feature authors add kotlinx libraries.
        return group == "org.jetbrains.kotlin" && module != null && module.startsWith("kotlin-stdlib")
    }

    private fun isFeature(path: String): Boolean = isFeatureApi(path) || isFeatureImpl(path)

    private fun isFeatureApi(path: String): Boolean = featureApi.matches(path)

    private fun isFeatureImpl(path: String): Boolean = featureImpl.matches(path)

    private fun isCore(path: String): Boolean = path.startsWith(":core:") && path.count { it == ':' } == 2
}

data class ModuleEdge(
    val from: String,
    val configuration: String,
    val toProject: String?,
    val group: String?,
    val module: String?,
) {
    fun links(from: String, toProject: String): Boolean = this.from == from && this.toProject == toProject
}
