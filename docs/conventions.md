# Naming glossary

Names are ours. From the reference project, take only the folder tree. If a name is in this file, do not introduce another.

## ViewModel

`ScreenViewModel<S : ScreenState, A : ScreenAction>`

Members: `state`, `currentState`, `createInitialState()`, `onAction()`, `reduce {}`, `launchCatching(onError) {}`.

`reduce {}` is the only place where the ViewModel changes State. Reducer functions are called inside the block. `launchCatching` does not swallow `CancellationException`. `onError` already receives `AppError`.

## State

`Loadable<T>`: `Loading`, `Ready(value)`, `Failed(error)`. A sealed interface, not a value class.

`UserMessage` is the snackbar. `<Screen>Sheet` is the screen sheet. Both are State fields, not an effect.

The screen triple: `<Screen>State`, `<Screen>Action`, `<Screen>Reducer`.

The reducer is an `object`. It holds `initial(...)` and `withX()` extension functions.

```kotlin
sealed interface AppError {
    val cause: Throwable
    data class NoConnection(override val cause: Throwable) : AppError
    data class Server(val statusCode: Int, override val cause: Throwable) : AppError
    data class Unexpected(override val cause: Throwable) : AppError
}

object TransactionsReducer {
    fun initial(data: LedgerData): TransactionsState = TransactionsState(
        items = Loadable.Loading,
        message = null,
        sheet = null,
    )

    fun TransactionsState.withItems(items: List<TransactionUiModel>) =
        copy(items = Loadable.Ready(items))

    fun TransactionsState.withError(error: AppError) =
        copy(items = Loadable.Failed(error))
}

// In the ViewModel. Extensions live on the object, so the reducer is the receiver:
// reduce { TransactionsReducer.run { withItems(items) } }
// launchCatching(onError = { error -> reduce { TransactionsReducer.run { withError(error) } } }) { ... }
```

## api

- `<Feature>Data` — data for starting the feature.
- `<Feature>Entry` — the entry point from other features.
- Domain models have no suffix. The name carries the area: `Transaction`, not `Item`.
- `<Name>Reader` and `<Name>Writer` — cross-feature data access. Example: `LedgerReader`.

A closed set of values is an `enum class` or a sealed interface in `api`, not string constants.

## impl

- `<Feature>Destination` — `@Serializable` sealed routes.
- `<Feature>Navigator` and `<Feature>NavigatorImpl`.
- `<Feature>Graph` — route registration in the NavHost.
- `<Screen>Host` — `koinViewModel` and `collectAsStateWithLifecycle`.
- `<Screen>Screen` — a pure function `(state, onAction)`.

## core:navigation

`Destination`, `RootNavigator`, `ComposeRootNavigator`.

## Model suffixes

| Layer | Name |
| --- | --- |
| Domain | no suffix |
| Room | `*Entity`, `*Dao` |
| Network | `*Dto` |
| UI | `*UiModel` |

Mappers: `toDomain()`, `toEntity()`, `toUiModel()`.

## Packages

- impl: `com.lebakin.finpulse.feature.<name>`
- api: `com.lebakin.finpulse.feature.<name>.api`
- core: `com.lebakin.finpulse.core.<name>`

## impl folders

```text
di/
domain/model
domain/repository
domain/usecase
data/repository
data/datasource/local
data/datasource/remote
data/datasource/showcase
data/mapper
presentation/navigation
presentation/model/mvi
presentation/model
presentation/mapper
presentation/viewmodel
presentation/host
presentation/view
presentation/uicache
```

`uicache` is created only when it is actually needed. `remote` does not appear in a feature until that feature has a network. Do not commit empty folders for symmetry.

## Build

The JDK for a local build is JBR 21 from Android Studio, through `JAVA_HOME`:

`/Users/glitchmymind/Applications/Android Studio.app/Contents/jbr/Contents/Home`

`sdk.dir` is written in `local.properties`. Both files are local and do not enter git.

## server

The server is a separate Gradle build in `server/`. It is not a mobile module. Package `com.lebakin.finpulse.server.<service>`.

| Piece | Name |
| --- | --- |
| Gateway project | `:api-gateway` |
| Ktor module | `Application.gateway()` |
| Process entry | `main` in `Gateway.kt` |
| Health route | `GET /health` returns the text `ok` |
| Server database project | `:database` in `server/` |
| Endpoint | `PostgresEndpoint` |
| Open | `openPostgres(primary, replica)` |
| Session | `Postgres` |
| Write | `Postgres.write` on `primary` |
| Read | `Postgres.read` on `replica` |

`main` binds `127.0.0.1` and port `8080`. Package for `:database` is `com.lebakin.finpulse.server.database`. A service or database project that is not in this table is not created ahead of its step.
