package com.lebakin.finpulse.feature.scanner.domain

internal class ReceiptSession {
    private var draft: ReceiptDraft? = null

    fun store(draft: ReceiptDraft) {
        this.draft = draft
    }

    fun current(): ReceiptDraft? = draft

    fun clear() {
        draft = null
    }
}
