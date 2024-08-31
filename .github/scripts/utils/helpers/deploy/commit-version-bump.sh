#!/bin/bash
# Copyright 2024 MusTalK (https://github.com/mustalk)
#
# Commit Version Bump Script
#
# This script commits the version bump and changelog updates.
# It adds the specified assets to be committed, creates a commit with the version name, release notes
# and pushes the changes to the remote branch.
#
# It is intended to be used in conjunction with semantic-release.
#
# Environment Variables:
#   REMOTE_NAME: The name of the remote repository (e.g., origin)
#   BRANCH_NAME: The name of the branch to push to (e.g., release)
#   ASSETS: A space-separated list of file paths to be committed (e.g., "app/build.gradle.kts CHANGELOG.md")
#
# Outputs:
#   SEMANTIC_RELEASE_STATUS: An environment variable indicating the status of the commit operation ('success' or 'failure').
#   SEMANTIC_RELEASE_MESSAGE: An environment variable providing a message describing the outcome of the commit operation.
#
# Usage:
#     ./commit-version-bump.sh
#
# Enable strict mode
set -euo pipefail

# Function to handle failures
handle_failure() {
    echo "ERROR: $1" >&2
    echo "SEMANTIC_RELEASE_STATUS=failure" >>"$GITHUB_ENV"
    echo "SEMANTIC_RELEASE_MESSAGE=$1" >>"$GITHUB_ENV"
    exit 1
}

# Function to handle success
handle_success() {
    echo "SUCCESS: $1" >&2
    echo "SEMANTIC_RELEASE_STATUS=success" >>"$GITHUB_ENV"
    echo "SEMANTIC_RELEASE_MESSAGE=$1" >>"$GITHUB_ENV"
    exit 0
}

# Function to handle info
handle_info() {
    echo "INFO: $1" >&2
}

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

# Add specified assets to be committed
# Arguments:
#   $@ - Array of file paths to be added to the commit
git_add() {
    local assets=("$@")
    handle_info "Adding assets: ${assets[*]}"

    # Add files to the staging area
    if ! git add "${assets[@]}"; then
        handle_failure "Version bump failed to add assets to git."
    fi
}

# Function to commit changes using a temporary file for the commit message
git_commit() {
    local next_release_version="$1"
    local release_notes="$2"

    # Create a temporary file
    local temp_commit_file
    temp_commit_file=$(mktemp)

    # Prepare the commit message
    local commit_message=""

    # Check if release_notes is empty and construct the commit message accordingly
    if [ -z "$release_notes" ]; then
        commit_message="chore(release): promote release v${next_release_version}"
    else
        commit_message="chore(release): promote release v${next_release_version} \n\n${release_notes}"
    fi

    # Write the commit message to the temporary file
    echo -e "$commit_message" >"$temp_commit_file"

    handle_info "Creating a new release commit..."

    # Run the git commit command using the temporary file to preserved the release_notes formatting as is.
    if ! git commit --file="$temp_commit_file"; then
        handle_failure "Version bump commit failed. See logs for details."
    fi

    # Clean up the temporary file
    rm -f "$temp_commit_file"
}

# Push the changes to the specified remote branch with force-with-lease
git_push() {
    local remote_name="$1"
    local branch_name="$2"
    handle_info "Pushing changes to $remote_name/$branch_name with --force-with-lease..."

    # Push the amended commit to the remote branch
    if git push "$remote_name" "$branch_name"; then
        handle_success "Version bump committed and pushed successfully."
    else
        handle_failure "Version bump push failed. See logs for details."
    fi
}

# Main function to execute the version bump commit process
# Arguments:
#   remote_name   - Remote repository name (e.g., origin)
#   branch_name   - Branch name to push to (e.g., release)
#   assets_string - Space-separated list of assets to commit (e.g., "app/build.gradle.kts CHANGELOG.md")
main() {
    # Define the required environment variables
    local required_vars=(REMOTE_NAME BRANCH_NAME NEXT_RELEASE_VERSION ASSETS)

    # Validate that required environment variables are set
    check_required_vars "${required_vars[@]}"

    # Capture environment variables
    local remote_name="$1"
    local branch_name="$2"
    local next_release_version="$3"
    local assets_string="$4"
    local release_notes="${5:-""}"

    # Convert space-separated string to array
    IFS=' ' read -r -a assets <<<"$assets_string"

    # Add assets to the commit
    git_add "${assets[@]}"

    # Create a commit with the version name and release notes
    git_commit "$next_release_version" "$release_notes"

    # Push the changes to the remote branch
    git_push "$remote_name" "$branch_name"
}

# Entry point
main "$REMOTE_NAME" "$BRANCH_NAME" "$NEXT_RELEASE_VERSION" "$ASSETS" "$RELEASE_NOTES"
