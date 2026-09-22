# FinPulse architecture

FinPulse is an offline app for tracking personal money. The first platforms are Android and iOS. Business logic is shared, in Kotlin Multiplatform. The interface is Compose Multiplatform. Cloud is added at the last stage and is not required to build and publish the app to the stores.

Root package, `applicationId`, and bundle id: `com.lebakin.finpulse`. The component showcase has its own id: `com.lebakin.finpulse.catalog`.

## What is already decided

- A build without a server (`finpulse.cloud.enabled=false`) contains no authorization. Account and sync modules are not linked into it.
- All feature business logic lives in `commonMain`. ViewModel and State contain no Compose types, so an iOS screen can later be rewritten in SwiftUI without touching the domain.
- Platform differences: an interface in `commonMain`, an implementation in `androidMain`, `iosMain`, or Swift, bound through Koin. `expect`/`actual` is only for small functions.
- MVI without effects. Navigation is hidden behind an interface. DTOs and Room entities do not leave `data`.
- No secrets in git.

## Why ViewModel does not close off SwiftUI

`androidx.lifecycle.ViewModel` exists in `commonMain` and builds for iOS. In SwiftUI it lives inside an `ObservableObject` wrapper: the wrapper holds a `ViewModelStore` and calls `clear()` in `deinit`. `StateFlow` is read through SKIE or through a `watch {}` helper in `iosMain`.

To keep that path open:

1. ViewModel and State have no Compose types.
2. `Loadable` is a sealed interface. A value class would arrive in Swift as `Any`.
3. `<Feature>Navigator` is an interface. SwiftUI gets its own implementation on `NavigationPath`.
4. Compose screens live in the same `impl`. If binary size starts to matter, they can move to a separate module.

## Document map

- [Modules and the graph](modules.md)
- [MVI, navigation, errors](presentation.md)
- [Data and DI](data.md)
- [UIKit and the showcase](ui.md)
- [The server, when its turn comes](server.md)
- [Naming glossary](../conventions.md)

The folder structure inside `impl` comes from a multi-module Android project and is described in the glossary. Class, method, and field names are not copied from there.
