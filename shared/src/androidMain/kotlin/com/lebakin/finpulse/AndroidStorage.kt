package com.lebakin.finpulse

import android.content.Context
import androidx.activity.ComponentActivity
import com.lebakin.finpulse.core.database.bindAndroidDatabase
import com.lebakin.finpulse.core.preferences.bindAndroidPreferencesDirectory
import com.lebakin.finpulse.feature.scanner.platform.attachAndroidReceiptCapture

fun prepareAndroidStorage(context: Context) {
    bindAndroidDatabase(context)
    bindAndroidPreferencesDirectory(context.filesDir.absolutePath)
}

fun attachReceiptCapture(activity: ComponentActivity) {
    attachAndroidReceiptCapture(activity)
}
