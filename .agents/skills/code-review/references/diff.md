# What to write at the end

Two lists.

Done: the files and why they changed.

Not checked: which command was not run, and why. A green detekt, once it exists, does not replace a look at module boundaries.

Do not write that a screen works if it was not built. Do not write that a secret is fine if gitleaks did not run on this diff: locally `.githooks/pre-commit` runs it, and in CI the `docs-and-secrets` workflow runs it.

If the build is still red after three attempts at one cause, the review does not close the task. Report first, as in the Fix phase.
