#!/bin/bash
set -euo pipefail

root=$(git rev-parse --show-toplevel)
pushd "$root" >/dev/null

echo "Updating local form-flow library..."
pushd "$root/../form-flow" >/dev/null
git pull
./gradlew build

popd >/dev/null
echo "Updating homeschool-pebt..."
git pull
