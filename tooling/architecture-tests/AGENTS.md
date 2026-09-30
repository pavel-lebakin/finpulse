# :tooling:architecture-tests

JVM module. Package `com.lebakin.finpulse.tooling.architecture`. It is not part of the app. Konsist reads the repository sources.

Production rules:

- Extensions of `object *Reducer` on a `*State` type are called only from `reduce {}`. `initial()` stays in `createInitialState()`.
- `Loadable` is the only sealed interface with that name, not a value class.
- `*Dto` and `*Entity` are declared and imported in a package with a `data` segment or in `com.lebakin.finpulse.core.database`.

Check: `scripts/check.sh :tooling:architecture-tests:test :tooling:architecture-tests:detekt`.
