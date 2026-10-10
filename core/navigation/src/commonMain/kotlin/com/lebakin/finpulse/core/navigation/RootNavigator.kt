package com.lebakin.finpulse.core.navigation

interface RootNavigator {
    fun navigate(destination: Destination)

    fun replace(destination: Destination)

    fun back()
}
