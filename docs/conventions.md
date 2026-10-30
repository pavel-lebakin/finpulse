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

## account

Cloud build only. `:shared` links it from `cloudMain` when `finpulse.cloud.enabled=true`.

| Piece | Name |
| --- | --- |
| Feature | `:feature:account` |
| Sync | `LedgerSync` |
| Change | `SyncChange` |
| Kind | `SyncKind` (`CATEGORY`, `TRANSACTION`) |
| Origin | `SyncOrigin` (`USER`, `SHOWCASE`) |
| Result | `SyncMerge`, `SyncConflict` |
| Outbox database | `OutboxDatabase` |
| Open outbox | `openOutboxDatabase` |
| Koin module | `accountModule` |

## server

The server is a separate Gradle build in `server/`. It is not a mobile module. Package `com.lebakin.finpulse.server.<service>`.

| Piece | Name |
| --- | --- |
| Gateway project | `:api-gateway` |
| Ktor module | `Application.gateway()` |
| Process entry | `main` in `Gateway.kt` |
| Health route | `GET /health` returns the text `ok` |
| Auth project | `:auth-service` in `server/` |
| Auth module | `Application.auth(accounts)` |
| Auth entry | `main` in `Auth.kt` |
| Sign-in route | `POST /sessions` |
| Account | `AuthAccount` |
| Verifier | `PasswordVerifier` |
| Build verifier | `passwordVerifier` |
| Check | `signIn` |
| Check result | `SignIn` (`Accepted`, `Rejected`) |
| Issue refresh token | `refreshToken` |
| Open refresh family | `refreshFamily` |
| Refresh family | `RefreshFamily` |
| Rotate | `rotate` |
| Rotate result | `Refresh` (`Rotated`, `Reused`, `Rejected`) |
| Save refresh family | `saveFamily` |
| Load refresh family | `loadFamily` |
| Server database project | `:database` in `server/` |
| Endpoint | `PostgresEndpoint` |
| Open | `openPostgres(primary, replica)` |
| Session | `Postgres` |
| Write | `Postgres.write` on `primary` |
| Read | `Postgres.read` on `replica` |
| Server cache project | `:cache` in `server/` |
| Cache endpoint | `RedisEndpoint` |
| Open cache | `openRedis(endpoint)` |
| Cache session | `Redis` |
| Cache write | `Redis.set` |
| Cache read | `Redis.get` |
| Server queue project | `:queue` in `server/` |
| Queue endpoint | `KafkaEndpoint` |
| Open queue | `openKafka(endpoint)` |
| Queue session | `Kafka` |
| Publish | `Kafka.publish` |
| Read | `Kafka.take` |
| Server storage project | `:storage` in `server/` |
| Storage endpoint | `R2Endpoint` |
| Open storage | `openR2(endpoint)` |
| Storage session | `R2` |
| Store object | `R2.put` |
| Signed URL | `R2.sign` |
| Server edge project | `:edge` in `server/` |
| Tunnel policy | `EdgeTunnel` |
| Shield policy | `EdgeShield` |
| Open edge | `openEdge(tunnel, shield)` |
| Edge session | `Edge` |
| Cached object | `EdgeObject` |
| Cache directive | `Edge.cacheControl` |

`main` in `Gateway.kt` binds `127.0.0.1` and port `8080`. `main` in `Auth.kt` binds `127.0.0.1` and port `8081`. Package for `:auth-service` is `com.lebakin.finpulse.server.auth`. Package for `:database` is `com.lebakin.finpulse.server.database`. Package for `:cache` is `com.lebakin.finpulse.server.cache`. Package for `:queue` is `com.lebakin.finpulse.server.queue`. Package for `:storage` is `com.lebakin.finpulse.server.storage`. Package for `:edge` is `com.lebakin.finpulse.server.edge`. A service, database, cache, queue, storage, or edge project that is not in this table is not created ahead of its step.
