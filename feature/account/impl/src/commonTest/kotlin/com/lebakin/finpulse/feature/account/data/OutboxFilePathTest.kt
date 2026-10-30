package com.lebakin.finpulse.feature.account.data

import kotlin.test.Test
import kotlin.test.assertEquals

class OutboxFilePathTest {
    @Test
    fun outboxSitsBesideTheLedgerFile() {
        assertEquals("/tmp/finpulse-outbox.db", outboxFilePath("/tmp/finpulse.db"))
    }
}
