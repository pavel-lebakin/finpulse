package com.lebakin.finpulse.feature.account.di

import com.lebakin.finpulse.core.database.databaseFilePath
import com.lebakin.finpulse.feature.account.api.LedgerSync
import com.lebakin.finpulse.feature.account.data.openOutboxDatabase
import com.lebakin.finpulse.feature.account.data.outboxFilePath
import com.lebakin.finpulse.feature.account.data.repository.RoomLedgerSync
import org.koin.dsl.module

val accountModule = module {
    single { openOutboxDatabase(outboxFilePath(databaseFilePath())) }
    single<LedgerSync> { RoomLedgerSync(get()) }
}
