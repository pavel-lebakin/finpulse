# Module boundaries

Until `checkModuleGraph` exists, check a new dependency by hand:

| From | Allowed | Not allowed |
| --- | --- | --- |
| `api` | `:core:common`, kotlinx | features, Compose, Room, Ktor |
| `impl` | its own `api`, another feature's `api`, the `core` it needs | another feature's `impl` |
| `core` | another `core`, toward `common` | any feature |
| a package outside `data` | — | `core.database` |

`:shared` sees every `impl` and wires their Koin modules. A feature does not wire another feature's Koin module itself.

A module `AGENTS.md` card is written with the module and answers four questions: why the module exists, which types are visible in `api`, which code is platform-specific, and which business rules must not be broken. The card does not copy this skill in full.
