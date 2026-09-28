package com.lebakin.finpulse

import android.app.Application
import com.lebakin.finpulse.di.initKoin

class FinPulseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin()
    }
}
