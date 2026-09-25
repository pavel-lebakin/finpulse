#!/usr/bin/env bash
# Checks agent-doc frontmatter, relative links, and file size limits.
set -euo pipefail

cd "$(dirname "$0")/.."

python3 - << 'PY'
import os
import re
import subprocess
import sys
from pathlib import Path

root = Path(".").resolve()
errors = []

def rel(path):
    return path.relative_to(root).as_posix()

def read(path):
    return path.read_text(encoding="utf-8")

agents = root / "AGENTS.md"
if not agents.is_file():
    errors.append("AGENTS.md is missing")
else:
    lines = read(agents).splitlines()
    if not 60 <= len(lines) <= 140:
        errors.append(f"AGENTS.md has {len(lines)} lines; expected 60-140 so it stays a router")

claude = root / "CLAUDE.md"
if not claude.is_file():
    errors.append("CLAUDE.md is missing")
else:
    text = read(claude).strip()
    if text != "@AGENTS.md":
        errors.append("CLAUDE.md must contain only @AGENTS.md")

link = root / ".claude" / "skills"
if not link.is_symlink():
    errors.append(".claude/skills must be a symlink")
elif os.readlink(link) != "../.agents/skills":
    errors.append(f".claude/skills points to {os.readlink(link)!r}, expected '../.agents/skills'")
elif not (root / ".agents" / "skills").is_dir():
    errors.append(".agents/skills is missing")

skills_root = root / ".agents" / "skills"
expected = {
    "finpulse-workflow",
    "feature-module",
    "mvi-screen",
    "data-layer",
    "uikit",
    "kmp-platform",
    "ai-features",
    "security-review",
    "code-review",
    "release",
}
if skills_root.is_dir():
    tracked = set()
    listed = subprocess.check_output(
        ["git", "ls-files", "--", ".agents/skills"],
        text=True,
    )
    for listed_path in listed.splitlines():
        parts = listed_path.split("/")
        if len(parts) >= 3 and parts[0] == ".agents" and parts[1] == "skills":
            tracked.add(parts[2])
    found = {name for name in tracked if (skills_root / name).is_dir()}
    missing = expected - found
    extra = found - expected
    if missing:
        errors.append("missing skills: " + ", ".join(sorted(missing)))
    if extra:
        errors.append("unexpected skills (server-service arrives at Stage 6): " + ", ".join(sorted(extra)))
    for name in sorted(found):
        skill = skills_root / name / "SKILL.md"
        if not skill.is_file():
            errors.append(f"{name}: SKILL.md is missing")
            continue
        text = read(skill)
        lines = text.splitlines()
        if len(lines) > 300:
            errors.append(f"{rel(skill)} has {len(lines)} lines; limit is 300")
        if not text.startswith("---\n"):
            errors.append(f"{rel(skill)}: frontmatter must start at line 1")
            continue
        end = text.find("\n---", 4)
        if end == -1:
            errors.append(f"{rel(skill)}: frontmatter is not closed")
            continue
        header = text[4:end]
        fields = {}
        for line in header.splitlines():
            if not line.strip():
                continue
            if ":" not in line:
                errors.append(f"{rel(skill)}: bad frontmatter line {line!r}")
                continue
            key, value = line.split(":", 1)
            fields[key.strip()] = value.strip()
        if fields.get("name") != name:
            errors.append(f"{rel(skill)}: name is {fields.get('name')!r}, directory is {name!r}")
        description = fields.get("description", "")
        if len(description) < 20:
            errors.append(f"{rel(skill)}: description is missing or too short")

link_re = re.compile(r"\[[^\]]+\]\(([^)]+)\)")
scan_roots = [root / "docs", root / ".agents", root / "AGENTS.md", root / "README.md", root / "CLAUDE.md"]
files = []
for item in scan_roots:
    if item.is_file():
        files.append(item)
    elif item.is_dir():
        files.extend(p for p in item.rglob("*.md") if p.is_file())

for path in files:
    for match in link_re.finditer(read(path)):
        target = match.group(1).strip()
        if target.startswith(("http://", "https://", "mailto:")):
            continue
        if target.startswith("#"):
            continue
        path_part, _, _anchor = target.partition("#")
        if path_part == "":
            continue
        resolved = (path.parent / path_part).resolve()
        if not resolved.is_file():
            errors.append(f"{rel(path)}: broken link {target}")

if errors:
    print("agent docs check failed:")
    for error in errors:
        print(f"  - {error}")
    sys.exit(1)

print(f"agent docs check passed ({len(files)} markdown files)")
PY
