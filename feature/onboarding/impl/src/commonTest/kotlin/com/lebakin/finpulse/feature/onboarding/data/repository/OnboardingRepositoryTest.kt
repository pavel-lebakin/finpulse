package com.lebakin.finpulse.feature.onboarding.data.repository

import com.lebakin.finpulse.core.preferences.OnboardingPreferences
import com.lebakin.finpulse.feature.ledger.api.Category
import com.lebakin.finpulse.feature.ledger.api.LedgerWriter
import com.lebakin.finpulse.feature.ledger.api.Transaction
import com.lebakin.finpulse.feature.onboarding.api.OnboardingChoice
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class OnboardingRepositoryTest {
    @Test
    fun sampleSeedsThenMarksCompleted() = runTest {
        val preferences = FakeOnboardingPreferences()
        val ledger = FakeLedgerWriter()
        val repository = OnboardingRepository(preferences, ledger)
        repository.complete(OnboardingChoice.Sample)
        assertEquals(listOf("seed"), ledger.calls)
        assertTrue(repository.observeCompleted().first())
    }

    @Test
    fun emptyClearsThenMarksCompleted() = runTest {
        val preferences = FakeOnboardingPreferences()
        val ledger = FakeLedgerWriter()
        val repository = OnboardingRepository(preferences, ledger)
        repository.complete(OnboardingChoice.Empty)
        assertEquals(listOf("clear"), ledger.calls)
        assertTrue(preferences.observeCompleted().first())
    }

    @Test
    fun failedSeedLeavesOnboardingIncomplete() = runTest {
        val preferences = FakeOnboardingPreferences()
        val ledger = FakeLedgerWriter(failSeed = true)
        val repository = OnboardingRepository(preferences, ledger)
        assertFailsWith<IllegalStateException> {
            repository.complete(OnboardingChoice.Sample)
        }
        assertFalse(preferences.observeCompleted().first())
    }

    @Test
    fun showAgainClearsTheCompletedFlag() = runTest {
        val preferences = FakeOnboardingPreferences()
        val repository = OnboardingRepository(preferences, FakeLedgerWriter())
        preferences.setCompleted(true)
        repository.showAgain()
        assertFalse(repository.observeCompleted().first())
    }
}

private class FakeOnboardingPreferences : OnboardingPreferences {
    private val completed = MutableStateFlow(false)

    override fun observeCompleted(): Flow<Boolean> = completed

    override suspend fun setCompleted(completed: Boolean) {
        this.completed.value = completed
    }
}

private class FakeLedgerWriter(
    private val failSeed: Boolean = false,
) : LedgerWriter {
    val calls = mutableListOf<String>()

    override suspend fun seedShowcase() {
        if (failSeed) {
            error("seed failed")
        }
        calls += "seed"
    }

    override suspend fun clearShowcase() {
        calls += "clear"
    }

    override suspend fun addUserTransaction(category: Category, transaction: Transaction): Boolean {
        calls += "user"
        return false
    }
}
