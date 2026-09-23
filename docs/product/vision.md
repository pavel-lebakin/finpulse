# Product

FinPulse shows where money goes, and it can create a transaction from a receipt or from a phrase. The app works without an account and without a server. Cloud is a later extension, not a condition of the first launch.

## Features

| Feature | What it does |
| --- | --- |
| `onboarding` | Four screens about the app and a choice of where to start |
| `ledger` | Transactions and categories |
| `insights` | Dashboard and charts |
| `scanner` | Camera, gallery, receipt samples, parsing into a transaction |
| `assistant` | Quick entry by phrase and answers about the user's own data |
| `settings` | Theme, data, AI mode, licenses |
| `account` | Cloud build only: sign-in and sync |

## Onboarding

Four screens, one promise per screen:

1. Offline and privacy. Data stays on the device until the user turns the cloud on.
2. Scanner. A receipt photo becomes a transaction draft.
3. AI assistant. A phrase becomes a draft. The write happens after confirmation.
4. Sample data. The user can look at the app with three months of history, or start from an empty list.

At the end, two actions: "See a sample" and "Start from an empty ledger".

Onboarding opens again from settings. Empty screens after the user declines the sample show a short hint of what to do next, not a blank page with no text.

## Showcase

If the user chooses the sample, Room stores a realistic three-month history. Each such row has `origin = SHOWCASE`.

The dashboard keeps a banner: "This is sample data. It is removed when you add your first transaction." The banner has a "Clear now" button.

The first user write, in one transaction, deletes every SHOWCASE row and turns showcase mode off. After that the user sees a message that the sample was removed.

"Clear now" does the same deletion without creating a transaction.

Showcase rows never enter sync. They are not user data and not a server demo mode.

## AI the user sees

The store build has three levels, from the more private one to the fallback:

1. On device. Android: Gemini Nano through ML Kit GenAI. iOS 26+: Apple Foundation Models. The Apple framework stays in Swift and reaches Koin through a Kotlin interface.
2. A deterministic rule-based receipt parser when there is no model.
3. The user's own Gemini key (BYOK), if the user entered it. Before the entry, a consent screen for sending data. Apple 5.1.2(i) and the Google Play Data safety form require that.

Receipt text and the model reply are untrusted. A write to the database happens only after confirmation on the review screen or in the assistant.

ML Kit GenAI Prompt API availability is checked at Stage 4. If the API is unavailable on the devices we need, the working path stays the parser and BYOK. The interface may promise an on-device model only when the engine is actually registered.
