# Data and DI

## Layer boundaries

DTOs and Room entities do not leave `data`. A domain model lives with no suffix. A UI model is named `*UiModel`.

Mappers between layers are extension functions in `data/mapper` and `presentation/mapper`: `toDomain()`, `toEntity()`, `toUiModel()`. A mapper is an `impl` detail, even when it is a pure function. It is not in `api`.

Cross-feature data access goes through `<Name>Reader` and `<Name>Writer` interfaces in `api`. Example: `LedgerReader`. Another feature's `impl` is not wired in for that.

## Showcase

Demo rows live in Room with `origin = SHOWCASE`. This is not a separate database and not a hardcoded list in a Composable. Display and deletion rules are in the [product vision](../product/vision.md). Showcase rows do not enter sync.

The source of those rows inside a feature is `data/datasource/showcase`. The `presentation/uicache` folder is created only when a screen really needs to survive leaving and returning. Do not create an empty folder.

## DI

Each Gradle module has its own Koin module. The graph is assembled in `:shared`. A test checks that the graph builds. That arrives with the composition root at Stage 2.

A platform implementation (OCR, Keystore, Keychain, the on-device model, Swift Foundation Models) is registered in Koin from the platform module. A feature depends on an interface in `commonMain`.

## Cloud outbox

Sync through a Room outbox, with conflict policies per data type, arrives at Stage 6 and only in a build with `finpulse.cloud.enabled=true`. Those classes are not in the offline binary. Do not add an empty outbox "for later" before that stage.
