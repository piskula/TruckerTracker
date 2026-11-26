#!/usr/bin/env bash
set -euo pipefail

script="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/prepare-release.sh"
fixture="$(mktemp -d)"
trap 'cd /; rm -rf "$fixture"' EXIT
cd "$fixture"
git init --quiet --initial-branch=main
git config user.name "Release workflow test"
git config user.email "release-test@example.invalid"
git config core.autocrlf false
mkdir app server shared
printf 'initial\n' > app/file
printf 'initial\n' > server/file
git add .
git commit --quiet -m initial
git tag v0.0.3
printf 'mobile\n' >> app/file
git commit --quiet -am mobile-change
printf 'server\n' >> server/file
git commit --quiet -am server-change
printf 'shared\n' > shared/file
git add .
git commit --quiet -m shared-change
git remote add origin .
git fetch --quiet origin main

export GITHUB_REF_TYPE=tag
export GITHUB_SERVER_URL=https://github.com
export GITHUB_REPOSITORY=example/release-test
export GITHUB_RUN_ID=1
export GITHUB_OUTPUT="$fixture/outputs"

prepare() {
    export GITHUB_REF_NAME="$2"
    git tag "$GITHUB_REF_NAME"
    : > "$GITHUB_OUTPUT"
    bash "$script" "$1"
}

reject() {
    local component="$1" tag="$2" message="$3"
    if prepare "$component" "$tag" > failure.log 2>&1; then
        printf 'Unexpectedly accepted %s\n' "$tag" >&2
        exit 1
    fi
    grep -F "$message" failure.log >/dev/null
    git tag -d "$tag" >/dev/null
}

prepare mobile mobile-0.0.4
grep -Fxq 'version-name=0.0.4' "$GITHUB_OUTPUT"
grep -Fxq 'version-code=4' "$GITHUB_OUTPUT"
grep -Fq 'Changes since v0.0.3:' release-notes.txt
grep -Fq mobile-change release-notes.txt
grep -Fq shared-change release-notes.txt
if grep -Fq server-change release-notes.txt; then exit 1; fi

prepare server server-1.0.0
grep -Fxq 'version-name=1.0.0' "$GITHUB_OUTPUT"
grep -Fxq "source-commit=$(git rev-parse HEAD)" "$GITHUB_OUTPUT"
grep -Fq server-change release-notes.txt
grep -Fq shared-change release-notes.txt
if grep -Fq mobile-change release-notes.txt; then exit 1; fi

reject mobile mobile-01.2.3 'without leading zeros'
reject server server-1.2 'without leading zeros'
reject mobile mobile-1.100.0 'MINOR and PATCH must be below 100'
reject mobile mobile-999999999999999999999.0.0 'MAJOR at most six digits'
reject mobile mobile-210000.0.1 'versionCode must be between'
reject mobile mobile-0.0.0 'versionCode must be between'
reject mobile mobile-0.0.2 'must be newer than mobile-0.0.4'
reject server server-0.9.9 'must be newer than server-1.0.0'
reject server server-1.1.0-rc1 'without leading zeros'

printf 'next server\n' >> server/file
git commit --quiet -am next-server-change
git fetch --quiet origin main
prepare server server-1.0.1
grep -Fq 'Changes since server-1.0.0:' release-notes.txt
grep -Fq next-server-change release-notes.txt
if grep -Fq mobile-change release-notes.txt; then exit 1; fi
prepare mobile mobile-1.2.3
grep -Fxq 'version-code=10203' "$GITHUB_OUTPUT"
grep -Fq 'Changes since mobile-0.0.4:' release-notes.txt
if grep -Fq next-server-change release-notes.txt; then exit 1; fi

git switch --quiet -c unmerged
printf 'unmerged\n' >> server/file
git commit --quiet -am unmerged-change
reject server server-1.1.0 'reachable from origin/main'

printf 'Release preparation checks passed\n'
