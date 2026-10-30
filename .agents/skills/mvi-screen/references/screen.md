# Building a screen

1. Describe State in full, including the message and the sheet. Do not leave "we will show it later through an effect".
2. One Action for each user action, including dismissing the message and the sheet.
3. Write `initial` and `withX()` so the reducer is a pure function and can be tested without a ViewModel.
4. Loading in the ViewModel looks like this: `launchCatching(onError = { reduce { withError(it) } })`. Success also goes through `reduce { withItems(...) }`.
5. `onError` receives `AppError`. `CancellationException` does not reach that callback.
6. Call navigation through a Navigator method, not from a Composable.
7. Host contains no business-logic branches. The branch lives in State.
8. Leave system insets to the app frame. Pad the screen with spacing tokens only.
9. Leave back, the section bar, and the menu to the app frame. Section changes slide in the frame pager. A screen does not draw a bottom "Back" label or a list of settings. Theme choices belong on the settings screen.
10. An onboarding page centers its picture and text and pins the button to the bottom.

While the repository has no second screen, the first screen is the sample. The next one copies its shape and does not invent a neighboring style.
