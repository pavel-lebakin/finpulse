---
name: feature-module
description: Use when creating a FinPulse feature module, its api and impl split, packages, or Koin module.
---

# Feature module

A feature is two Gradle modules:

- `:feature:<name>:api`
- `:feature:<name>:impl`

Feature names: `onboarding`, `ledger`, `insights`, `scanner`, `assistant`, `settings`. `account` only arrives with the cloud.

## api

Package `com.lebakin.finpulse.feature.<name>.api`.

Only what another feature may see: `<Feature>Data`, `<Feature>Entry`, domain models, `<Name>Reader`, and `<Name>Writer`. Dependencies: `:core:common` and kotlinx. Room, Ktor, Compose, and other features do not belong here.

## impl

Package `com.lebakin.finpulse.feature.<name>`.

Depends on its own `api`, on other features' `api`, and on the `core` modules it needs. It does not depend on another feature's `impl`. Its Koin module is registered in `:shared`, not in another feature.

Folder tree and class names: [docs/conventions.md](../../../docs/conventions.md). Do not create empty directories. `data/datasource/remote` appears together with the network. `presentation/uicache` appears only when a screen must keep state across leaving and returning.

## group

Module directories are named `api` and `impl` for every feature. Set a unique Gradle `group`, for example `com.lebakin.finpulse.feature.ledger`, or the klib names collide.

Module skeleton and graph check: [references/boundaries.md](references/boundaries.md).
