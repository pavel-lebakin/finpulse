---
name: ai-features
description: Use when implementing FinPulse receipt parsing, the on-device model, BYOK Gemini, prompt checks, or the assistant.
---

# AI in the product

Three steps, chosen in this order:

1. On-device model. Android: Gemini Nano through ML Kit GenAI. iOS 26+: Foundation Models in Swift. Kotlin receives an interface.
2. A rule-based receipt parser when there is no model.
3. The user's Gemini key (BYOK), after a separate consent screen.

A transaction is not written from model text or OCR by itself. The user confirms the draft. The assistant answers questions through our read-only use cases, not through free text that changes something.

Receipt text and the model reply are untrusted. The reply is accepted after a schema check.

Product rules: [docs/product/vision.md](../../../docs/product/vision.md). Key storage: [docs/security/guidelines.md](../../../docs/security/guidelines.md).

Chain and prohibitions: [references/chain.md](references/chain.md).
