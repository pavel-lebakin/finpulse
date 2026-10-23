package com.lebakin.finpulse.feature.assistant.domain

import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal fun interface PhraseClock {
    fun today(): LocalDate
}

internal class SystemPhraseClock : PhraseClock {
    override fun today(): LocalDate =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
}

internal class SystemPhraseIds : PhraseIds {
    override fun next(): String = "user-${kotlin.random.Random.nextLong().toULong()}"
}
