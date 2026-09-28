package com.lebakin.finpulse.core.testing

import com.lebakin.finpulse.core.navigation.Destination
import kotlin.test.Test
import kotlin.test.assertEquals

class FakeRootNavigatorTest {
    @Test
    fun recordsNavigation() {
        val navigator = FakeRootNavigator()
        navigator.navigate(SampleDestination)
        navigator.back()
        val opened: List<Destination> = listOf(SampleDestination)
        assertEquals(opened, navigator.opened)
        assertEquals(1, navigator.backCount)
    }
}

private data object SampleDestination : Destination
