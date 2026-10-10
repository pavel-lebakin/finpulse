package com.lebakin.finpulse.core.testing

import com.lebakin.finpulse.core.navigation.Destination
import com.lebakin.finpulse.core.navigation.RootNavigator

class FakeRootNavigator : RootNavigator {
    val opened = mutableListOf<Destination>()
    val replaced = mutableListOf<Destination>()
    var backCount: Int = 0
        private set

    override fun navigate(destination: Destination) {
        opened += destination
    }

    override fun replace(destination: Destination) {
        replaced += destination
    }

    override fun back() {
        backCount += 1
    }
}
