#!/usr/bin/env bash
# Prepares a macOS or Linux machine for local development (see `just setup`).
# macOS: installs missing tools with Homebrew. Linux: checks and tells you what is missing.
# Safe to run repeatedly: anything already in place is skipped.
set -euo pipefail
cd "$(dirname "$0")/.."

REQUIRED_JAVA=25
REQUIRED_NODE=22
installed=false

step() { printf '\n==> %s\n' "$1"; }
ok() { printf '  [ok] %s\n' "$1"; }
warn() { printf '  [!!] %s\n' "$1"; }

java_major() {
    local exe=java
    [ -n "${JAVA_HOME:-}" ] && [ -x "$JAVA_HOME/bin/java" ] && exe="$JAVA_HOME/bin/java"
    "$exe" -version 2>&1 | sed -n 's/.*version "\([0-9]*\).*/\1/p' | head -n1 || true
}
node_major() { node --version 2>/dev/null | sed -n 's/^v\([0-9]*\).*/\1/p' || true; }

install() { # install <name> <brew args...>
    local name=$1; shift
    if [ "$(uname)" = "Darwin" ] && command -v brew >/dev/null; then
        read -r -p "  $name is missing or too old. Install it with Homebrew? [Y/n] " answer
        case "$answer" in n|N|no) echo "$name is required."; exit 1 ;; esac
        brew install "$@"
        installed=true
        ok "$name installed"
    else
        echo "  $name is missing or too old. Install it with your package manager (Linux) or install Homebrew first (macOS)."
        exit 1
    fi
}

step "JDK $REQUIRED_JAVA"
jm=$(java_major)
if [ "${jm:-0}" -ge "$REQUIRED_JAVA" ]; then ok "Java $jm"; else install "JDK $REQUIRED_JAVA" --cask "temurin@$REQUIRED_JAVA"; fi

step "Node.js $REQUIRED_NODE or newer"
nm=$(node_major)
if [ "${nm:-0}" -ge "$REQUIRED_NODE" ]; then ok "Node $nm"; else install "Node.js" node; fi

step ".env"
if [ -f .env ]; then ok ".env already exists"; else cp .env.example .env; ok ".env created from .env.example"; fi

if [ "$installed" = true ]; then
    echo
    echo "Tools were installed. Open a NEW terminal and run 'just setup' again to finish."
    exit 0
fi

step "Frontend dependencies"
(cd frontend && npm install)
ok "npm packages installed"

step "Backend dependencies"
(cd backend && ./mvnw -q -B dependency:go-offline)
ok "Maven dependencies downloaded"

# Docker is not installed by this script. It is only checked here, last,
# so the warning is the final thing you read.
step "Docker"
docker_ready=false
if ! command -v docker >/dev/null; then
    warn "Docker is NOT installed. Install Docker (e.g. Docker Desktop: https://www.docker.com/products/docker-desktop/) before running 'just dev'."
elif ! docker info >/dev/null 2>&1; then
    warn "Docker is installed but not running. Start it before running 'just dev'."
else
    ok "$(docker --version)"
    docker compose pull
    ok "Images pulled"
    docker_ready=true
fi

echo
if [ "$docker_ready" = true ]; then
    echo "Setup finished. Start developing with: just dev"
else
    echo "Setup finished, but Docker is not ready (see above). Fix that, then start with: just dev"
fi
