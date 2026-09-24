# Mapping

The repository accepts and returns domain models. It may depend on a Dao and on a local source. It does not return an Entity and does not accept a DTO from outside the `data` module.

A presentation mapper does not touch the database. It receives a domain model and builds `*UiModel`, including amount text already formatted when the formatter is shared. Do not concatenate the amount by hand in a Composable.

Showcase and a user record are not split across two databases. Deleting the sample and inserting the first user operation is one Room transaction. After it, showcase mode is off.

Importing `com.lebakin.finpulse.core.database` is allowed only from a package whose path contains a `data` segment. Do not pass a Dao into a ViewModel.

Do not add a sync outbox before Stage 6. Until then the local database is the only source of truth.
