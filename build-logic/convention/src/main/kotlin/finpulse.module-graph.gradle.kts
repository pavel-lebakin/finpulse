import com.lebakin.finpulse.gradle.ModuleEdge
import com.lebakin.finpulse.gradle.ModuleGraphRules
import org.gradle.api.GradleException
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.ProjectDependency

// Module scripts add dependencies during project evaluation. The snapshot is
// taken after that, then the task reads plain strings.
val moduleEdges = mutableListOf<ModuleEdge>()
val modulePaths = mutableSetOf<String>()

gradle.projectsEvaluated {
    moduleEdges.clear()
    modulePaths.clear()
    rootProject.subprojects.forEach { module ->
        modulePaths += module.path
        module.configurations.forEach { configuration ->
            if (!ModuleGraphRules.isProductionConfiguration(configuration.name)) return@forEach
            configuration.dependencies.forEach { dependency ->
                when (dependency) {
                    is ProjectDependency -> moduleEdges += ModuleEdge(
                        from = module.path,
                        configuration = configuration.name,
                        toProject = dependency.path,
                        group = null,
                        module = null,
                    )
                    is ExternalModuleDependency -> moduleEdges += ModuleEdge(
                        from = module.path,
                        configuration = configuration.name,
                        toProject = null,
                        group = dependency.group,
                        module = dependency.name,
                    )
                }
            }
        }
    }
}

tasks.register("checkModuleGraph") {
    group = "verification"
    description = "Fails when a module dependency crosses a feature, core, or catalog boundary."
    doLast {
        val problems = ModuleGraphRules.violations(moduleEdges.toList(), modulePaths.toSet())
        if (problems.isNotEmpty()) {
            throw GradleException("Module graph:\n${problems.joinToString("\n")}")
        }
    }
}
