package com.lebakin.finpulse.tooling.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoBaseDeclaration
import com.lemonappdev.konsist.api.declaration.KoFunctionDeclaration
import com.lemonappdev.konsist.api.declaration.KoObjectDeclaration
import com.lemonappdev.konsist.api.ext.list.withName
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.provider.KoContainingDeclarationProvider
import com.lemonappdev.konsist.api.verify.assertEmpty
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue as assertThat

class ArchitectureTest {
    @Test
    fun loadableIsASealedInterface() {
        val production = Konsist.scopeFromProduction()
        val loadable = production.interfaces().withName("Loadable")
        assertEquals(1, loadable.size)
        loadable.assertTrue { it.hasSealedModifier }
        production.classes().withName("Loadable").assertEmpty()
    }

    @Test
    fun reducerExtensionsAreCalledOnlyInsideReduce() {
        val production = Konsist.scopeFromProduction()
        val names = reducerExtensionNames(production.objects())
        production.functions()
            .filterNot { it.declaredInsideReducer() }
            .assertTrue { ReducerCalls.calledOnlyInsideReduce(it.text, names) }
    }

    @Test
    fun sampleReducerStaysInsideReduce() {
        val tests = Konsist.scopeFromSourceSet("jvmTest")
        val names = reducerExtensionNames(tests.objects())
        assertThat(names.contains("withReady"))
        assertThat(names.contains("withError"))
        tests.functions()
            .filterNot { it.declaredInsideReducer() }
            .assertTrue(strict = true) { ReducerCalls.calledOnlyInsideReduce(it.text, names) }
    }

    @Test
    fun dtoAndEntityStayInData() {
        val production = Konsist.scopeFromProduction()
        production.classesAndInterfacesAndObjects()
            .filter { it.name.endsWith("Dto") || it.name.endsWith("Entity") }
            .assertTrue { DataModels.keptInData(it.packagee?.name) }
        production.files.assertTrue { file ->
            file.imports
                .filter { DataModels.importNamesModel(it.name) }
                .all { DataModels.keptInData(file.packagee?.name) }
        }
    }

    private fun reducerExtensionNames(objects: List<KoObjectDeclaration>): Set<String> =
        objects
            .withNameEndingWith("Reducer")
            .flatMap { reducer -> reducer.functions(includeNested = false, includeLocal = false) }
            .filter { function ->
                function.isExtension && function.receiverType?.name?.endsWith("State") == true
            }
            .map { it.name }
            .toSet()

    private fun KoFunctionDeclaration.declaredInsideReducer(): Boolean {
        val seen = mutableSetOf<KoBaseDeclaration>()
        var current: KoBaseDeclaration? = containingDeclaration
        var inside = false
        while (current != null && seen.add(current)) {
            if (current is KoObjectDeclaration && current.name.endsWith("Reducer")) {
                inside = true
            }
            current = (current as? KoContainingDeclarationProvider)?.containingDeclaration
        }
        return inside
    }
}
