#!/usr/bin/env bash
# Run the given Gradle tasks and print a short result.
# The full log stays in build/agent/last.log.
# Usage: scripts/check.sh :core:common:jvmTest :shared:compileKotlinIosSimulatorArm64
set -euo pipefail

root="$(cd "$(dirname "$0")/.." && pwd)"
cd "$root"

if [[ $# -eq 0 ]]; then
  echo "Usage: scripts/check.sh <gradle-task>..." >&2
  exit 2
fi

if [[ -z "${JAVA_HOME:-}" ]]; then
  export JAVA_HOME="/Users/glitchmymind/Applications/Android Studio.app/Contents/jbr/Contents/Home"
fi

mkdir -p build/agent
log="build/agent/last.log"

set +e
./gradlew --console=plain "$@" >"$log" 2>&1
code=$?
set -e

if [[ "$code" -eq 0 ]]; then
  echo "OK: $*"
  exit 0
fi

echo "FAILED: $*"
python3 - "$log" << 'PY'
import sys
lines = open(sys.argv[1], encoding="utf-8", errors="replace").read().splitlines()
shown = []
capture = False
for line in lines:
    if line.startswith("* What went wrong"):
        capture = True
    if capture:
        if line.startswith("* Try"):
            break
        shown.append(line)
        continue
    if "FAILED" in line or line.startswith("e: ") or line.startswith("w: file://"):
        shown.append(line)
tail = shown[:40]
print("\n".join(tail))
if len(shown) > len(tail):
    print(f"... {len(shown) - len(tail)} more lines in build/agent/last.log")
PY
exit "$code"
