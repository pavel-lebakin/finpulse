---
name: kmp-platform
description: Use when adding Android, iOS, or Swift code, an expect declaration, or a Koin binding for a platform API in FinPulse.
---

# Platform

A large platform piece is an interface in `commonMain` and an implementation in `androidMain`, `iosMain`, or Swift. Koin binds them in the platform module. A small thing such as a path format is `expect fun`.

Do not wrap a Swift-only API (Foundation Models) in a fake Kotlin framework. Kotlin sees the interface. Swift implements it and passes it into Koin.

The ViewModel stays in `commonMain` without Compose types, so a SwiftUI screen can hold the same model. The Navigator for SwiftUI is a separate implementation of the same interface on `NavigationPath`.

The build of a touched KMP module includes `compileKotlinIosSimulatorArm64`, not only Android.

How to choose expect or an interface: [references/split.md](references/split.md).
