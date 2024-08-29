#!/bin/bash
# Copyright 2024 MusTalK (https://github.com/mustalk)
#
# Get Release ID Script
#
# This script fetches the GitHub Release ID for a specified tag using the GitHub API.
#
# Environment Variables:
#   BOT_GITHUB_TOKEN: GitHub token with repository permissions.
#   GITHUB_REPOSITORY: GitHub repository name (e.g., username/repo).
#   CURRENT_RELEASE_VERSION: The version number of the release (e.g., 1.0.0).
#
# Outputs:
#   RELEASE_ID: The ID of the GitHub release for the specified tag.
#
# Usage:
#   ./get_release_id.sh
#
# Enable strict mode
set -euo pipefail

# Function to check if all required environment variables are set
check_required_vars() {
    local vars=("$@")

    # Check if each required variable is set
    for var in "${vars[@]}"; do
        if [ -z "${!var}" ]; then
            handle_failure "Environment variable $var is not set."
        fi
    done
}

# Function to handle failures
handle_failure() {
    echo "ERROR: $1" >&2
    exit 1
}

# Function to handle info
handle_info() {
    echo "INFO: $1" >&2
}

# Function to fetch the release ID for the specified tag
get_release_id() {
    local bot_github_token="$1"
    local github_repository="$2"
    local current_release_version="$3"

    # Construct the tag to look for from the current version
    local tag="v${current_release_version}"
    handle_info "Fetching release ID for tag $tag"

    # Fetch release ID using GitHub API
    local release_id
    release_id=$(curl -s -H "Authorization: token $bot_github_token" \
        "https://api.github.com/repos/$github_repository/releases/tags/$tag" | jq -r .id)

    # Check if release ID is valid
    if [ "$release_id" == "null" ]; then
        handle_failure "No release found for tag $tag"
    fi

    # Export the release ID to GitHub Actions environment
    echo "RELEASE_ID=$release_id" >>"$GITHUB_ENV"
}

# Main function to execute the get release id process
main() {
    # Define the required environment variables
    local required_vars=(BOT_GITHUB_TOKEN GITHUB_REPOSITORY CURRENT_RELEASE_VERSION)

    # Validate that required environment variables are set
    check_required_vars "${required_vars[@]}"

    # Capture environment variables
    local bot_github_token="$1"
    local github_repository="$2"
    local current_release_version="$3"

    # Get the release id for the given version
    get_release_id "$bot_github_token" "$github_repository" "$current_release_version"
}

# Entry point
main "$BOT_GITHUB_TOKEN" "$GITHUB_REPOSITORY" "$CURRENT_RELEASE_VERSION"
