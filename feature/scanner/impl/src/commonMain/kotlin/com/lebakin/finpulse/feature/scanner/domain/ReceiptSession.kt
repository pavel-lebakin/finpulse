package com.lebakin.finpulse.feature.scanner.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class ReceiptSession {
    private var generation = 0L
    private val held = MutableStateFlow<Held?>(null)

    fun store(draft: ReceiptDraft) {
        generation += 1
        held.value = Held(generation = generation, draft = draft)
    }

    fun current(): ReceiptDraft? = held.value?.draft

    fun observe(): Flow<ReceiptDraft?> = held.map { it?.draft }

    fun clear() {
        held.value = null
    }

    private data class Held(
        val generation: Long,
        val draft: ReceiptDraft,
    )
}
