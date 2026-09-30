package com.lebakin.finpulse.tooling.architecture

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DataModelsTest {
    @Test
    fun featureDataPackageKeepsModels() {
        assertTrue(DataModels.keptInData("com.lebakin.finpulse.feature.ledger.data.datasource.local"))
    }

    @Test
    fun databasePackageKeepsModels() {
        assertTrue(DataModels.keptInData(DataModels.DATABASE_PACKAGE))
        assertTrue(DataModels.keptInData("${DataModels.DATABASE_PACKAGE}.local"))
    }

    @Test
    fun presentationAndDomainDoNotKeepModels() {
        assertFalse(DataModels.keptInData("com.lebakin.finpulse.feature.ledger.presentation.viewmodel"))
        assertFalse(DataModels.keptInData("com.lebakin.finpulse.feature.ledger.domain.model"))
        assertFalse(DataModels.keptInData("com.lebakin.finpulse.feature.ledger.api"))
        assertFalse(DataModels.keptInData(null))
    }

    @Test
    fun modelImportsAreRecognized() {
        assertTrue(DataModels.importNamesModel("com.lebakin.finpulse.feature.ledger.data.TransactionDto"))
        assertTrue(DataModels.importNamesModel("com.lebakin.finpulse.core.database.TransactionEntity"))
        assertTrue(DataModels.importNamesModel("com.lebakin.finpulse.feature.ledger.data.*"))
        assertFalse(DataModels.importNamesModel("com.lebakin.finpulse.core.common.Loadable"))
        assertFalse(DataModels.importNamesModel("com.lebakin.finpulse.feature.ledger.presentation.*"))
    }
}
