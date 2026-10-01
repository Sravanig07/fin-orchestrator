#!/usr/bin/env bash
# FinOrchestrator Maven Wrapper helper script
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if command -v mvn >/dev/null 2>&1; then
    MVN_CMD="mvn"
elif [ -x "/Applications/IntelliJ IDEA.app/Contents/plugins/maven-plugin/lib/maven3/bin/mvn" ]; then
    MVN_CMD="/Applications/IntelliJ IDEA.app/Contents/plugins/maven-plugin/lib/maven3/bin/mvn"
elif [ -x "/opt/homebrew/bin/mvn" ]; then
    MVN_CMD="/opt/homebrew/bin/mvn"
else
    echo "Error: Maven not found in PATH or standard macOS paths." >&2
    exit 1
fi

exec "$MVN_CMD" "$@"
