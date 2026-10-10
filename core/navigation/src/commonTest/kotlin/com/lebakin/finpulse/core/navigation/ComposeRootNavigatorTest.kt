package com.lebakin.finpulse.core.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ComposeRootNavigatorTest {
    @Test
    fun commandsWaitUntilTheHostIsAttached() {
        val navigator = ComposeRootNavigator()
        val received = mutableListOf<RootCommand>()
        navigator.navigate(SampleDestination)
        navigator.back()
        navigator.attach { command -> received += command }
        assertEquals(2, received.size)
        assertEquals(SampleDestination, assertIs<RootCommand.Open>(received[0]).destination)
        assertIs<RootCommand.Back>(received[1])
    }

    @Test
    fun replaceWaitsUntilTheHostIsAttached() {
        val navigator = ComposeRootNavigator()
        val received = mutableListOf<RootCommand>()
        navigator.replace(SampleDestination)
        navigator.attach { command -> received += command }
        val replace = assertIs<RootCommand.Replace>(received.single())
        assertEquals(SampleDestination, replace.destination)
    }

    @Test
    fun detachQueuesTheNextCommand() {
        val navigator = ComposeRootNavigator()
        val first = mutableListOf<RootCommand>()
        val second = mutableListOf<RootCommand>()
        navigator.attach { command -> first += command }
        navigator.navigate(SampleDestination)
        navigator.detach()
        navigator.back()
        navigator.attach { command -> second += command }
        assertEquals(1, first.size)
        assertIs<RootCommand.Open>(first.single())
        assertIs<RootCommand.Back>(second.single())
    }
}

private data object SampleDestination : Destination
