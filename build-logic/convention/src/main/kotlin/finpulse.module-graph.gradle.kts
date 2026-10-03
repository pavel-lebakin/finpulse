import com.lebakin.finpulse.gradle.MappedModule
import com.lebakin.finpulse.gradle.ModuleEdge
import com.lebakin.finpulse.gradle.ModuleGraphRules
import com.lebakin.finpulse.gradle.ProjectMap
import org.gradle.api.GradleException
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.ProjectDependency
import java.io.File
import kotlin.io.path.relativeTo

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

val projectMapFile = layout.projectDirectory.file("docs/project-map.md")

fun projectMapText(): String {
    val root = rootProject.projectDir.toPath()
    fun relative(file: File): String = file.toPath().relativeTo(root).toString().replace('\\', '/')
    fun cardFile(start: File): File? {
        var dir = start.toPath().normalize()
        val stop = root.normalize()
        while (dir != stop && dir.startsWith(stop)) {
            val candidate = dir.resolve("AGENTS.md").toFile()
            if (candidate.isFile) return candidate
            dir = dir.parent ?: break
        }
        return null
    }

    val missing = mutableListOf<String>()
    val modules = mutableListOf<MappedModule>()
    val cards = linkedMapOf<String, String>()
    fun addCard(card: File) {
        val path = relative(card)
        if (path !in cards) {
            cards[path] = card.readText().replace("\r\n", "\n")
        }
    }

    rootProject.subprojects.sortedBy { it.path }.forEach { module ->
        val dir = module.projectDir
        val hasBuildScript = dir.resolve("build.gradle.kts").isFile || dir.resolve("build.gradle").isFile
        if (!hasBuildScript) return@forEach
        val card = cardFile(dir)
        if (card == null) {
            missing += module.path
        } else {
            addCard(card)
            modules += MappedModule(module.path, relative(dir), relative(card))
        }
    }
    if (missing.isNotEmpty()) {
        throw GradleException(
            "Module cards missing:\n${missing.joinToString("\n")}\nEach Gradle module needs an AGENTS.md in its directory or a parent directory.",
        )
    }

    rootProject.projectDir.listFiles()
        ?.filter { it.isDirectory && !it.name.startsWith(".") }
        ?.sortedBy { it.name }
        ?.forEach { dir ->
            val card = dir.resolve("AGENTS.md")
            if (!card.isFile) return@forEach
            val path = relative(card)
            if (path in cards) return@forEach
            addCard(card)
            modules += MappedModule(null, relative(dir), path)
        }

    return ProjectMap.render(modules, moduleEdges.toList(), cards)
}

tasks.register("projectMap") {
    group = "documentation"
    description = "Writes docs/project-map.md from the Gradle graph and module AGENTS.md cards."
    doLast {
        projectMapFile.asFile.writeText(projectMapText())
    }
}

tasks.register("checkProjectMap") {
    group = "verification"
    description = "Fails when docs/project-map.md does not match the Gradle graph and module cards."
    doLast {
        val expected = projectMapText()
        val file = projectMapFile.asFile
        if (!file.isFile) {
            throw GradleException("docs/project-map.md is missing. Run ./gradlew projectMap.")
        }
        val actual = file.readText().replace("\r\n", "\n")
        if (actual != expected) {
            throw GradleException("docs/project-map.md is stale. Run ./gradlew projectMap.")
        }
    }
}
