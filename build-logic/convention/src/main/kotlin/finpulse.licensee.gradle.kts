import app.cash.licensee.LicenseeExtension
import app.cash.licensee.UnusedAction

plugins {
    id("app.cash.licensee")
}

extensions.configure<LicenseeExtension>("licensee") {
    allow("Apache-2.0")
    allow("MIT")
    allow("BSD-2-Clause")
    allow("BSD-3-Clause")
    // The allowlist is the policy. An identifier that no current dependency uses is not a violation.
    unusedAction(UnusedAction.IGNORE)
}
