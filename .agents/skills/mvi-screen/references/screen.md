# Building a screen

1. Describe State in full, including the message and the sheet. Do not leave "we will show it later through an effect".
2. One Action for each user action, including dismissing the message and the sheet.
3. Write `initial` and `withX()` so the reducer is a pure function and can be tested without a ViewModel.
4. Loading in the ViewModel looks like this: `launchCatching(onError = { reduce { withError(it) } })`. Success also goes through `reduce { withItems(...) }`.
5. `onError` receives `AppError`. `CancellationException` does not reach that callback.
6. Call navigation through a Navigator method, not from a Composable.
7. Host contains no business-logic branches. The branch lives in State.

While the repository has no second screen, the first screen is the sample. The next one copies its shape and does not invent a neighboring style.
