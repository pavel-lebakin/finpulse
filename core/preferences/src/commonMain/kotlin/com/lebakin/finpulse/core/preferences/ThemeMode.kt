package com.lebakin.finpulse.core.preferences

enum class ThemeMode {
    System,
    Light,
    Dark,
    ;

    fun stored(): String = when (this) {
        System -> SYSTEM
        Light -> LIGHT
        Dark -> DARK
    }

    companion object {
        fun fromStored(value: String?): ThemeMode = when (value) {
            LIGHT -> Light
            DARK -> Dark
            else -> System
        }
    }
}

private const val SYSTEM = "system"

private const val LIGHT = "light"

private const val DARK = "dark"
