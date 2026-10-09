package com.lebakin.finpulse.feature.ledger.di

import com.lebakin.finpulse.feature.ledger.api.LedgerReader
import com.lebakin.finpulse.feature.ledger.api.LedgerWriter
import com.lebakin.finpulse.feature.ledger.data.repository.RoomLedgerRepository
import com.lebakin.finpulse.feature.ledger.data.repository.SystemLedgerClock
import org.koin.dsl.module

val ledgerModule = module {
    single { RoomLedgerRepository(get(), get(), SystemLedgerClock()) }
    single<LedgerReader> { get<RoomLedgerRepository>() }
    single<LedgerWriter> { get<RoomLedgerRepository>() }
}
