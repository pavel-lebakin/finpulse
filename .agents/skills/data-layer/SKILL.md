---
name: data-layer
description: Use when adding Room, DataStore, DTO, repositories, mappers, or showcase rows in a FinPulse feature.
---

# Data layer

DTOs and `*Entity` stay in `data`. What leaves is a domain model with no suffix. The UI receives `*UiModel` from `presentation/mapper`.

Mapping functions: `toDomain()`, `toEntity()`, `toUiModel()`. They live in `impl`, not in `api`.

Another feature reads data through `<Name>Reader` or changes it through `<Name>Writer`. Those interfaces are in `api`. The implementation is in `data/repository`.

Showcase rows are written to the same database with `origin = SHOWCASE`. The prefill source is `data/datasource/showcase`. They are not part of sync.

Showcase rules: [docs/product/vision.md](../../../docs/product/vision.md). Layer boundaries: [docs/architecture/data.md](../../../docs/architecture/data.md).

What does not belong in the repository: [references/mapping.md](references/mapping.md).
