#!/usr/bin/env bash
# Checks .github/workflows/codeql.yml analyzes Kotlin with a traced Gradle build.
set -euo pipefail

cd "$(dirname "$0")/.."

python3 - << 'PY'
import re
import sys
from pathlib import Path

root = Path(".").resolve()
workflow = root / ".github" / "workflows" / "codeql.yml"
errors = []

required_tasks = (
    "-p build-logic",
    ":convention:compileKotlin",
    ":androidApp:assembleDebug",
    ":catalog:android:assembleDebug",
    ":catalog:desktop:compileKotlin",
    ":tooling:architecture-tests:compileKotlin",
    ":shared:compileKotlinJvm",
    ":catalog:catalogShared:compileKotlinJvm",
    ":core:common:compileKotlinJvm",
    ":core:mvi:compileKotlinJvm",
    ":core:navigation:compileKotlinJvm",
    ":core:testing:compileKotlinJvm",
    ":core:uikit:compileKotlinJvm",
)
required_actions = (
    "actions/checkout",
    "actions/setup-java",
    "android-actions/setup-android",
    "github/codeql-action/init",
    "github/codeql-action/analyze",
)
uses_line = re.compile(r"^\s*uses:\s+(\S+)@([0-9a-f]{40})\s+#\s+\S+\s*$")


if not workflow.is_file():
    errors.append(".github/workflows/codeql.yml is missing")
    text = ""
else:
    text = workflow.read_text(encoding="utf-8")

if "\t" in text:
    errors.append("tab is not allowed")
if "pull_request_target" in text:
    errors.append("pull_request_target is not allowed")
if re.search(r"build-mode:\s*none\b", text):
    errors.append("build-mode none does not analyze Kotlin")
if "compileKotlinIos" in text:
    errors.append("the CodeQL job has no Xcode, so it must not compile iOS")

lines = text.splitlines()
seen_actions = set()
init_line = analyze_line = gradle_line = java_line = sdk_line = None
for line_number, raw in enumerate(lines, start=1):
    stripped = raw.strip()
    if stripped.startswith("uses:"):
        match = uses_line.match(raw)
        if match is None:
            errors.append(f"line {line_number}: action must be a 40-character SHA pin with a version comment")
            continue
        action = match.group(1)
        seen_actions.add(action)
        if action == "github/codeql-action/init":
            init_line = line_number
        elif action == "github/codeql-action/analyze":
            analyze_line = line_number
        elif action == "actions/setup-java":
            java_line = line_number
        elif action == "android-actions/setup-android":
            sdk_line = line_number
    if "./gradlew" in raw and gradle_line is None:
        gradle_line = line_number

for action in required_actions:
    if action not in seen_actions:
        errors.append(f"missing action {action}")

if "security-events: write" not in text:
    errors.append("security-events: write is missing")
if not re.search(r"(?m)^ {2}push:\s*$", text):
    errors.append("on.push is missing")
if not re.search(r"(?m)^ {2}pull_request:\s*$", text):
    errors.append("on.pull_request is missing")
if not re.search(r"(?m)^ {10}languages:\s+java-kotlin\s*$", text):
    errors.append("languages must be java-kotlin")
if not re.search(r"(?m)^ {10}build-mode:\s+manual\s*$", text):
    errors.append("build-mode must be manual")
if not re.search(r"(?m)^ {10}java-version:\s+21\s*$", text):
    errors.append("java-version must be 21")
if "--no-daemon" not in text:
    errors.append("the Gradle build must pass --no-daemon")
for task in required_tasks:
    if task not in text:
        errors.append(f"missing Gradle task {task}")

if init_line is None or gradle_line is None or analyze_line is None:
    errors.append("init, ./gradlew, and analyze must all be present")
elif not (init_line < gradle_line < analyze_line):
    errors.append("order must be CodeQL init, then ./gradlew, then analyze")
elif java_line is None or sdk_line is None or not (java_line < init_line and sdk_line < init_line):
    errors.append("JDK and Android SDK setup must run before CodeQL init")

if errors:
    print("codeql check failed:")
    for error in errors:
        print(f"  - {error}")
    sys.exit(1)

print("codeql check passed (java-kotlin, manual Gradle build)")
PY
