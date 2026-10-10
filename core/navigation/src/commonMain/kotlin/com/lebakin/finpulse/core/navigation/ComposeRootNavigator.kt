package com.lebakin.finpulse.core.navigation

sealed interface RootCommand {
    data class Open(val destination: Destination) : RootCommand

    data class Replace(val destination: Destination) : RootCommand

    data object Back : RootCommand
}

class ComposeRootNavigator : RootNavigator {
    private val pending = ArrayDeque<RootCommand>()
    private var sink: ((RootCommand) -> Unit)? = null

    fun attach(sink: (RootCommand) -> Unit) {
        this.sink = sink
        while (pending.isNotEmpty()) {
            sink(pending.removeFirst())
        }
    }

    fun detach() {
        sink = null
    }

    override fun navigate(destination: Destination) {
        dispatch(RootCommand.Open(destination))
    }

    override fun replace(destination: Destination) {
        dispatch(RootCommand.Replace(destination))
    }

    override fun back() {
        dispatch(RootCommand.Back)
    }

    private fun dispatch(command: RootCommand) {
        val current = sink
        if (current == null) {
            pending.addLast(command)
        } else {
            current(command)
        }
    }
}
