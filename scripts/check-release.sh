#!/usr/bin/env bash
# Checks the manual Android release workflow and the Pages privacy policy.
set -euo pipefail

cd "$(dirname "$0")/.."

python3 - << 'PY'
import re
import sys
from pathlib import Path

root = Path(".").resolve()
errors = []
uses_line = re.compile(r"^\s*uses:\s+(\S+)@([0-9a-f]{40})\s+#\s+\S+\s*$")


def read(relative):
    path = root / relative
    if not path.is_file():
        errors.append(f"{relative} is missing")
        return ""
    text = path.read_text(encoding="utf-8")
    if "\t" in text and relative.endswith((".yml", ".yaml")):
        errors.append(f"{relative}: tab is not allowed")
    return text


def check_pins(relative, text):
    for line_number, raw in enumerate(text.splitlines(), start=1):
        if raw.strip().startswith("uses:"):
            if uses_line.match(raw) is None:
                errors.append(
                    f"{relative}:{line_number}: action must be a 40-character SHA pin with a version comment"
                )


release = read(".github/workflows/release-android.yml")
pages = read(".github/workflows/pages.yml")
gradle = read("androidApp/build.gradle.kts")
plist = read("iosApp/iosApp/Info.plist")
privacy = read("iosApp/iosApp/PrivacyInfo.xcprivacy")
policy = read("docs/legal/index.html")
gitignore = read(".gitignore")

check_pins(".github/workflows/release-android.yml", release)
check_pins(".github/workflows/pages.yml", pages)

if "pull_request_target" in release or "pull_request_target" in pages:
    errors.append("pull_request_target is not allowed")
if not re.search(r"(?m)^on:\n  workflow_dispatch:\s*$", release):
    errors.append("release-android must be workflow_dispatch only")
if re.search(r"(?m)^  (push|pull_request):", release):
    errors.append("release-android must not run on push or pull_request")
for secret in (
    "ANDROID_KEYSTORE_BASE64",
    "ANDROID_KEYSTORE_PASSWORD",
    "ANDROID_KEY_ALIAS",
    "ANDROID_KEY_PASSWORD",
):
    if f"secrets.{secret}" not in release:
        errors.append(f"release-android must reference secrets.{secret}")
if re.search(r"(?i)(password|keystore)\s*[:=]\s*['\"][^$'\"\n{][^'\"]*['\"]", release):
    errors.append("release-android must not embed a password or a keystore")
if ":androidApp:bundleRelease" not in release:
    errors.append("release-android must bundle the release")

if "docs/legal" not in pages:
    errors.append("pages must publish docs/legal")
if "actions/deploy-pages" not in pages:
    errors.append("pages must deploy with actions/deploy-pages")
if "pages: write" not in pages or "id-token: write" not in pages:
    errors.append("pages needs pages: write and id-token: write")

if "isMinifyEnabled = true" not in gradle or "isShrinkResources = true" not in gradle:
    errors.append("the Android release build must enable R8 and resource shrinking")
if "keystore.properties" not in gradle:
    errors.append("release signing must read keystore.properties")
if "keystore.properties" not in gitignore:
    errors.append("keystore.properties must stay gitignored")

if "<key>ITSAppUsesNonExemptEncryption</key>" not in plist or "<false/>" not in plist.split("ITSAppUsesNonExemptEncryption", 1)[-1][:80]:
    errors.append("ITSAppUsesNonExemptEncryption must be false")
if "NSCameraUsageDescription" not in plist or "NSPhotoLibraryUsageDescription" not in plist:
    errors.append("camera and photo usage descriptions are required")
if "NSPrivacyCollectedDataTypeOtherUserContent" not in privacy:
    errors.append("PrivacyInfo.xcprivacy must list other user content")
if "NSPrivacyTracking" not in privacy or "<false/>" not in privacy.split("NSPrivacyTracking", 1)[-1][:40]:
    errors.append("NSPrivacyTracking must be false")
if "<h1>FinPulse privacy policy</h1>" not in policy:
    errors.append("docs/legal/index.html must be the privacy policy")

if errors:
    print("release check failed:")
    for error in errors:
        print(f"  - {error}")
    sys.exit(1)

print("release check passed (R8, signing secrets, privacy policy)")
PY
