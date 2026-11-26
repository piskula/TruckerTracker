#!/usr/bin/env bash
set -euo pipefail

component="${1:?Expected server or mobile}"
case "$component" in
    server)
        paths=(server shared gradle/libs.versions.toml .github/workflows/build-server.yml .github/scripts)
        ;;
    mobile)
        paths=(app shared gradle/libs.versions.toml .github/workflows/release-app.yml .github/scripts)
        ;;
    *)
        echo "::error::Expected server or mobile"
        exit 1
        ;;
esac

version_pattern='(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)'
if [[ "${GITHUB_REF_TYPE:-}" != tag || ! "$GITHUB_REF_NAME" =~ ^${component}-${version_pattern}$ ]]; then
    echo "::error::Expected ${component}-MAJOR.MINOR.PATCH without leading zeros"
    exit 1
fi

major="${BASH_REMATCH[1]}"
minor="${BASH_REMATCH[2]}"
patch="${BASH_REMATCH[3]}"
version="$major.$minor.$patch"
version_code=""
if [[ "$component" == mobile ]]; then
    if [[ ${#major} -gt 6 || ${#minor} -gt 2 || ${#patch} -gt 2 ]]; then
        echo "::error::Mobile MINOR and PATCH must be below 100 and MAJOR at most six digits"
        exit 1
    fi
    version_code=$((major * 10000 + minor * 100 + patch))
    if (( version_code < 1 || version_code > 2100000000 )); then
        echo "::error::Mobile versionCode must be between 1 and 2100000000"
        exit 1
    fi
fi

commit="$(git rev-parse --verify "refs/tags/${GITHUB_REF_NAME}^{commit}")"
if [[ "$commit" != "$(git rev-parse HEAD)" ]] || ! git merge-base --is-ancestor "$commit" origin/main; then
    echo "::error::Release tags must point to the checked-out commit reachable from origin/main"
    exit 1
fi

patterns=("$component-*")
if [[ "$component" == mobile ]]; then
    patterns+=('v*')
fi
previous="$(
    git tag --list "${patterns[@]}" |
        while IFS= read -r candidate; do
            if [[ "$candidate" == "$GITHUB_REF_NAME" ]]; then
                continue
            fi
            candidate_version="${candidate#"$component"-}"
            if [[ "$component" == mobile ]]; then
                candidate_version="${candidate_version#v}"
            fi
            if [[ "$candidate_version" =~ ^${version_pattern}$ ]]; then
                printf '%s %s\n' "$candidate_version" "$candidate"
            fi
        done |
        sort -k1,1V |
        tail -n 1
)"
previous_tag=""
if [[ -n "$previous" ]]; then
    read -r previous_version previous_tag <<< "$previous"
    highest="$(printf '%s\n%s\n' "$version" "$previous_version" | sort -V | tail -n 1)"
    if [[ "$highest" != "$version" || "$version" == "$previous_version" ]]; then
        echo "::error::Release version must be newer than $previous_tag"
        exit 1
    fi
fi

{
    if [[ -n "$previous_tag" ]]; then
        printf 'Changes since %s:\n\n' "$previous_tag"
        git log --pretty=format:'- %s' "${previous_tag}..${GITHUB_REF_NAME}" -- "${paths[@]}"
    else
        printf 'Initial %s release:\n\n' "$component"
        git log --pretty=format:'- %s' "$GITHUB_REF_NAME" -- "${paths[@]}"
    fi
    printf '\n\nCommit: `%s`\n' "$commit"
    printf 'Workflow run: %s/%s/actions/runs/%s\n' "$GITHUB_SERVER_URL" "$GITHUB_REPOSITORY" "$GITHUB_RUN_ID"
} > release-notes.txt

{
    printf 'version-name=%s\n' "$version"
    printf 'version-code=%s\n' "$version_code"
    printf 'source-commit=%s\n' "$commit"
} >> "$GITHUB_OUTPUT"
