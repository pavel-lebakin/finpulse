package com.lebakin.finpulse.feature.assistant.domain

internal class PhraseSession {
    private var draft: PhraseDraft? = null

    fun store(draft: PhraseDraft) {
        this.draft = draft
    }

    fun current(): PhraseDraft? = draft

    fun clear() {
        draft = null
    }
}
