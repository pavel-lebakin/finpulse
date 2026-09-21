# Security policy

FinPulse is a personal source-available project. There is no bug-bounty
program and no public production deployment yet.

## Report a vulnerability

Email glitchofmymind@gmail.com with the subject `FinPulse security`.

Include what you found, where it is, and how to reproduce it. Do not open a
public issue or pull request for a vulnerability. Do not include live API
keys, tokens, or personal data in the report.

## What to expect

The author reads reports personally. There is no guaranteed response time.
A confirmed issue will be fixed in the repository before any store release
that contains the affected code.

## Secrets

The git history must not contain signing keys, API keys, tokens, or
passwords. Those belong in gitignored files, environment variables, or
GitHub Actions secrets. CI runs gitleaks on every push and pull request.
A local pre-commit hook runs the same scan when `gitleaks` is installed.

If a secret is committed, rotate it. Deleting the file in a later commit
does not remove it from history.
