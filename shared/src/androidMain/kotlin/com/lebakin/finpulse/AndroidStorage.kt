package com.lebakin.finpulse

import android.content.Context
import com.lebakin.finpulse.core.database.bindAndroidDatabase
import com.lebakin.finpulse.core.preferences.bindAndroidPreferencesDirectory

fun prepareAndroidStorage(context: Context) {
    bindAndroidDatabase(context)
    bindAndroidPreferencesDirectory(context.filesDir.absolutePath)
}
