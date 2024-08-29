#!/bin/bash
# Copyright 2024 MusTalK (https://github.com/mustalk)
#
# Upload APK Script
#
# This script uploads an APK file to GitHub Release Assets using the GitHub API.
#
# Environment Variables:
#   BOT_GITHUB_TOKEN: GitHub token with repository permissions.
#   GITHUB_REPOSITORY: GitHub repository name(e.g., username/repo).
#   CURRENT_RELEASE_VERSION: The version number of the release (e.g., 1.0.0).
#   APK_PATH: The path to the APK file to be uploaded.
#   RELEASE_ID: The ID of the GitHub release.
#
# Usage:
#   ./upload_apk.sh
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

# Function to upload APK to GitHub Release Assets
upload_apk() {
    local bot_github_token="$1"
    local github_repository="$2"
    local current_release_version="$3"
    local apk_path="$4"
    local release_id="$5"

    # Extract repository name and convert to lowercase
    local repo_name="${github_repository#*/}"
    repo_name="${repo_name,,}"

    # Construct the APK name using the repository name and version name
    local apk_name="${repo_name}-${current_release_version}-release.apk"

    # Log the APK name being uploaded
    handle_info "Uploading $apk_path as $apk_name"

    # Upload the APK file to GitHub Release Assets using the release ID
    curl -s -H "Authorization: token $bot_github_token" \
        -H "Content-Type: application/octet-stream" \
        --data-binary @"$apk_path" \
        "https://uploads.github.com/repos/$github_repository/releases/$release_id/assets?name=$apk_name"
}

# Main function to execute the upload apk process
main() {
    # Define the required environment variables
    local required_vars=(BOT_GITHUB_TOKEN GITHUB_REPOSITORY CURRENT_RELEASE_VERSION APK_PATH RELEASE_ID)

    # Validate that required environment variables are set
    check_required_vars "${required_vars[@]}"

    # Capture environment variables
    local bot_github_token="$1"
    local github_repository="$2"
    local current_release_version="$3"
    local apk_path="$4"
    local release_id="$5"

    # Upload the APK file to the GitHub Release Assets
    upload_apk "$bot_github_token" "$github_repository" "$current_release_version" "$apk_path" "$release_id"
}

# Entry point
main "$BOT_GITHUB_TOKEN" "$GITHUB_REPOSITORY" "$CURRENT_RELEASE_VERSION" "$APK_PATH" "$RELEASE_ID"
