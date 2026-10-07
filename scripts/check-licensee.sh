#!/usr/bin/env bash
# Checks Licensee is applied with the permissive allowlist and that CI runs it.
set -euo pipefail

cd "$(dirname "$0")/.."

python3 - << 'PY'
import re
import sys
from pathlib import Path

root = Path(".").resolve()
errors = []

catalog = root / "gradle" / "libs.versions.toml"
convention = root / "build-logic" / "convention" / "build.gradle.kts"
plugin = root / "build-logic" / "convention" / "src" / "main" / "kotlin" / "finpulse.licensee.gradle.kts"
workflow = root / ".github" / "workflows" / "mobile.yml"
appliers = (
    root / "build-logic" / "convention" / "src" / "main" / "kotlin" / "finpulse.kmp.library.gradle.kts",
    root / "build-logic" / "convention" / "src" / "main" / "kotlin" / "finpulse.android.application.gradle.kts",
    root / "build-logic" / "convention" / "src" / "main" / "kotlin" / "finpulse.jvm.application.gradle.kts",
    root / "build-logic" / "convention" / "src" / "main" / "kotlin" / "finpulse.jvm.library.gradle.kts",
)
required_allows = (
    "Apache-2.0",
    "MIT",
    "BSD-2-Clause",
    "BSD-3-Clause",
)
forbidden_allow = re.compile(
    r"GPL|LGPL|AGPL|SSPL|EUPL|MPL|EPL|CDDL|BUSL|OSL|CPAL",
    re.IGNORECASE,
)


def read(path: Path) -> str:
    if not path.is_file():
        errors.append(f"{path.relative_to(root).as_posix()} is missing")
        return ""
    text = path.read_text(encoding="utf-8")
    if "\t" in text:
        errors.append(f"{path.relative_to(root).as_posix()}: tab is not allowed")
    return text


catalog_text = read(catalog)
if catalog_text and not re.search(r'(?m)^licensee\s*=\s*"[^"]+"\s*$', catalog_text):
    errors.append("version catalog is missing the licensee version")
if catalog_text and "app.cash.licensee:licensee-gradle-plugin" not in catalog_text:
    errors.append("version catalog is missing app.cash.licensee:licensee-gradle-plugin")

convention_text = read(convention)
if convention_text and "libs.licensee.gradlePlugin" not in convention_text:
    errors.append("build-logic convention does not depend on the Licensee plugin")

plugin_text = read(plugin)
if plugin_text:
    if 'id("app.cash.licensee")' not in plugin_text:
        errors.append("finpulse.licensee does not apply app.cash.licensee")
    if "violationAction" in plugin_text:
        errors.append("violationAction must stay the default FAIL")
    if "ignoreDependencies" in plugin_text:
        errors.append("ignoreDependencies hides a dependency from the license check")
    if "allowUrl" in plugin_text or "allowDependency" in plugin_text:
        errors.append("allowUrl and allowDependency need a reviewed exception, not a blanket entry")
    allows = re.findall(r'allow\("([^"]+)"\)', plugin_text)
    for spdx in required_allows:
        if spdx not in allows:
            errors.append(f"allowlist is missing {spdx}")
    for spdx in allows:
        if forbidden_allow.search(spdx):
            errors.append(f"allowlist contains a copyleft identifier: {spdx}")
    if allows and set(allows) != set(required_allows):
        extra = sorted(set(allows) - set(required_allows))
        if extra:
            errors.append("allowlist has identifiers outside the policy: " + ", ".join(extra))
    if "unusedAction(UnusedAction.IGNORE)" not in plugin_text:
        errors.append("unused policy identifiers must be ignored, not warned on every build")

for path in appliers:
    text = read(path)
    if text and 'id("finpulse.licensee")' not in text:
        errors.append(f"{path.name} does not apply finpulse.licensee")

workflow_text = read(workflow)
if workflow_text and not re.search(r"(?m)^\s+licensee\s*$", workflow_text):
    errors.append("mobile workflow must run the licensee task")

if errors:
    print("licensee check failed:")
    for error in errors:
        print(f"  - {error}")
    sys.exit(1)

print("licensee check passed (Apache-2.0, MIT, BSD-2-Clause, BSD-3-Clause)")
PY
