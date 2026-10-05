#!/usr/bin/env bash
# Checks .github/dependabot.yml names the Gradle and GitHub Actions updates.
set -euo pipefail

cd "$(dirname "$0")/.."

python3 - << 'PY'
import sys
from pathlib import Path

root = Path(".").resolve()
config = root / ".github" / "dependabot.yml"
errors = []

required = {
    ("gradle", "/"),
    ("gradle", "/build-logic"),
    ("github-actions", "/"),
}
allowed_intervals = {"daily", "weekly", "monthly"}


def unquote(value):
    value = value.strip()
    if len(value) >= 2 and value[0] == value[-1] and value[0] in {'"', "'"}:
        return value[1:-1]
    return value


def parse_updates(text):
    version = None
    updates = []
    current = None
    in_schedule = False
    schedule_indent = None
    seen_updates = False

    for line_number, raw in enumerate(text.splitlines(), start=1):
        if raw.strip() == "" or raw.lstrip().startswith("#"):
            continue
        if "\t" in raw:
            errors.append(f"line {line_number}: tab is not allowed")
            continue
        indent = len(raw) - len(raw.lstrip(" "))
        stripped = raw.strip()
        if ":" not in stripped:
            errors.append(f"line {line_number}: expected a key")
            continue
        key, _, value = stripped.partition(":")
        key = key.strip()
        if key.startswith("- "):
            key = key[2:].strip()
            if key != "package-ecosystem":
                errors.append(f"line {line_number}: an update must start with package-ecosystem")
                continue
            current = {"package-ecosystem": unquote(value), "line": line_number}
            updates.append(current)
            in_schedule = False
            seen_updates = True
            continue
        if indent == 0 and key == "version":
            version = unquote(value)
            continue
        if indent == 0 and key == "updates":
            seen_updates = True
            continue
        if current is None:
            errors.append(f"line {line_number}: unexpected key {key!r}")
            continue
        if key == "schedule" and value.strip() == "":
            in_schedule = True
            schedule_indent = indent
            current["schedule"] = {}
            continue
        if in_schedule and indent > schedule_indent:
            current["schedule"][key] = unquote(value)
            continue
        in_schedule = False
        if value.strip() == "":
            errors.append(f"line {line_number}: {key} needs a value")
            continue
        current[key] = unquote(value)

    if not seen_updates:
        errors.append("updates: is missing")
    return version, updates


if not config.is_file():
    errors.append(".github/dependabot.yml is missing")
    version, updates = None, []
else:
    version, updates = parse_updates(config.read_text(encoding="utf-8"))

if version != "2":
    errors.append(f"version is {version!r}; expected 2")

found = set()
for update in updates:
    ecosystem = update.get("package-ecosystem")
    directory = update.get("directory")
    interval = update.get("schedule", {}).get("interval") if isinstance(update.get("schedule"), dict) else None
    pair = (ecosystem, directory)
    if pair in found:
        errors.append(f"duplicate update {ecosystem} {directory}")
    found.add(pair)
    if pair not in required:
        errors.append(
            f"line {update.get('line')}: unexpected update {ecosystem} {directory}"
        )
    if interval not in allowed_intervals:
        errors.append(
            f"line {update.get('line')}: schedule.interval is {interval!r}"
        )
    if ecosystem == "gradle":
        project = root if directory == "/" else root / directory.lstrip("/")
        if not (project / "settings.gradle.kts").is_file():
            errors.append(f"{directory} has no settings.gradle.kts")
    elif ecosystem == "github-actions":
        workflows = root / ".github" / "workflows"
        if not any(workflows.glob("*.yml")):
            errors.append(f"{directory} has no GitHub Actions workflows")

missing = required - found
for ecosystem, directory in sorted(missing):
    errors.append(f"missing update {ecosystem} {directory}")

catalog = root / "gradle" / "libs.versions.toml"
wrapper = root / "gradle" / "wrapper" / "gradle-wrapper.properties"
if not catalog.is_file():
    errors.append("gradle/libs.versions.toml is missing")
if not wrapper.is_file():
    errors.append("gradle/wrapper/gradle-wrapper.properties is missing")

if errors:
    print("dependabot check failed:")
    for error in errors:
        print(f"  - {error}")
    sys.exit(1)

print(f"dependabot check passed ({len(updates)} update entries)")
PY
