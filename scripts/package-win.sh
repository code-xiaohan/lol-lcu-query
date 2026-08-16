#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

./mvnw -DskipTests package
cd frontend
npm install
npm run build
cd "$ROOT"
./mvnw -DskipTests package
cd frontend
npm run electron:pack
