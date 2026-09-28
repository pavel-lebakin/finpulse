package com.lebakin.finpulse.catalog

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "FinPulse Catalog") {
        CatalogApp()
    }
}
