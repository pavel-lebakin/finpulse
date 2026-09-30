package com.lebakin.finpulse.tooling.architecture

internal object DataModels {
    const val DATABASE_PACKAGE = "com.lebakin.finpulse.core.database"

    fun keptInData(packageName: String?): Boolean {
        if (packageName == null) {
            return false
        }
        return packageName.split('.').any { it == "data" } ||
            packageName == DATABASE_PACKAGE ||
            packageName.startsWith("$DATABASE_PACKAGE.")
    }

    fun importNamesModel(importName: String): Boolean {
        val simpleName = importName.substringAfterLast('.')
        val namedModel = simpleName.endsWith("Dto") || simpleName.endsWith("Entity")
        val wildcardOfData = simpleName == "*" && keptInData(importName.removeSuffix(".*"))
        return namedModel || wildcardOfData
    }
}
