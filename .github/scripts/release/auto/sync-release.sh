#!/bin/bash
# Copyright 2024 MusTalK (https://github.com/mustalk)
#
# Auto Sync Release Script
#
# This script automates the synchronization of the release branch with the main branch within a CI/CD pipeline.
#
# For more details, see the README.md file in this directory and the DEV_WORKFLOW.md file at the project root.
#
# Enable strict mode
set -euo pipefail

# Set the SCRIPT_OPERATION_MODE variable before sourcing core-utils.sh.
# This variable should be set to "merge" or "sync" depending on the operation mode.
export SCRIPT_OPERATION_MODE="sync"

# Enable / Disable debug mode for the logs
export DEBUG="true"

# Source the core utilities script.
source .github/scripts/utils/auto/core-utils.sh

# List of required environment variables.
REQUIRED_VARS=(RELEASE_BRANCH MAIN_BRANCH REMOTE_NAME)

# Function to check if all commits from release are already in main.
# This function checks whether the main branch is up-to-date with the release branch.
# If there are no new commits to merge, it logs a success message and exits.
check_commits_up_to_date() {
    local main_branch=$1
    local release_branch=$2
    local remote_name=$3
    local operation_mode=$4
    local ahead_commits=""
    local commit_range=""

    # Get the commit range between the main and release branches.
    commit_range=$(get_commit_range "$main_branch" "$release_branch" "$remote_name")

    # Get the list of commits ahead in the release branch
    ahead_commits="$(git log "$commit_range" --oneline)"

    # If no commits are ahead, the branches are already up-to-date.
    if [[ -z "$ahead_commits" ]]; then
        # Check for commit history differences.
        if check_commit_history_diff "$main_branch" "$release_branch" "$remote_name"; then
            handle_success "No $operation_mode needed, \`$remote_name/$release_branch\` is already up-to-date with \`$remote_name/$main_branch\`." false
        else
            handle_info "Commit history differs between \`$remote_name/$release_branch\` and \`$remote_name/$main_branch\`. A $operation_mode is required."
        fi
    else
        handle_info "Differences in file contents between \`$remote_name/$release_branch\` and \`$remote_name/$main_branch\`. A $operation_mode is required."
    fi
}

# Function to handle differences between the release and main branches.
# and decide on the appropriate actions based on the detected differences.
handle_branch_diffs() {
    local release_branch=$1
    local main_branch=$2
    local remote_name=$3
    local operation_mode=$4
    local diff_message=""

    # Check for differences between the release and main branches,
    # and capture the output in the diff_message variable.
    diff_message=$(get_branch_diffs "$release_branch" "$main_branch" "$remote_name" "$operation_mode")

    # Based on the content of diff_message, decide on the appropriate course of action.
    # If the message contains "INFO:", log it as an informational message.
    # If the message contains "WARNING:", treat it as a failure and exit.
    if [[ -n "$diff_message" ]]; then
        if echo "$diff_message" | grep -q "INFO:"; then
            handle_info "$diff_message"
        elif echo "$diff_message" | grep -q "WARNING:"; then
            handle_failure "$diff_message"
        fi
    fi
}

# Function to synchronize the release branch with the main branch by performing a rebase.
# This function attempts to rebase the release branch onto the main branch.
start_release_sync_process() {
    local main_branch=$1
    local release_branch=$2
    local remote_name=$3

    # Attempt to rebase the release branch onto the main branch.
    # If the rebase is successful, proceed to push the changes.
    # If the rebase fails, trigger a failure handler to log the error and exit.
    if run_cmd git rebase "$main_branch"; then
        push_release_branch "$main_branch" "$release_branch" "$remote_name"
    else
        handle_failure "Failed to rebase \`$release_branch\` branch onto \`$main_branch\`. Please check and resolve manually."
    fi
}

# Function to push the rebased release branch to the remote repository.
# The changes are force-pushed to ensure that the remote branch is updated
# with the rebased history from the main branch.
push_release_branch() {
    local main_branch=$1
    local release_branch=$2
    local remote_name=$3

    # Force push the rebased release branch to the remote repository using the --force-with-lease option.
    # If the push is successful, trigger the success handler.
    # If the push fails, trigger a failure handler to log the error and exit.
    if run_cmd git push "$remote_name" "$release_branch" --force-with-lease; then
        handle_success "Successfully synchronized the \`$release_branch\` branch with \`$main_branch\`."
    else
        handle_failure "Failed to push changes to the \`$release_branch\` branch. Please check and resolve manually."
    fi
}

# Main function to execute the sync process.
# This is the entry point of the script.
main() {
    local remote_name="$1"
    local main_branch="$2"
    local release_branch="$3"
    local operation_mode="$4"

    # Set up logging for the script.
    # This ensures that all relevant output is captured in a log file.
    setup_logging

    # Check if all required environment variables are set.
    # This prevents the script from running if crucial variables are missing.
    check_required_vars "${REQUIRED_VARS[@]}"

    # Ensure that the script is running in the root directory of the repository.
    # This is necessary to avoid path issues during Git operations.
    ensure_repo_root

    # Fetch the latest changes from the remote repository.
    # This ensures that the local branches are up-to-date with the remote.
    fetch_remote_branch "$remote_name"

    # Check if the main branch has any new commits that need to be synced with release branch.
    # If the release branch is already up-to-date with main, the script will exit early with a success message.
    check_commits_up_to_date "$main_branch" "$release_branch" "$remote_name" "$operation_mode"

    # Handle any differences found between the branches.
    handle_branch_diffs "$release_branch" "$main_branch" "$remote_name" "$operation_mode"

    # Log an informational message indicating the start of the sync process.
    handle_info "Starting $operation_mode of \`$release_branch\` with \`$main_branch\` ..."

    # Ensure that the working directory is clean before proceeding.
    # This prevents conflicts caused by untracked files or changes in the working directory.
    ensure_clean_working_directory

    # Checkout and track the main branch from the remote.
    checkout_and_track_branch "$main_branch" "$remote_name"

    # Checkout and track the release branch from the remote.
    checkout_and_track_branch "$release_branch" "$remote_name"

    # Start the process to synchronize the release branch with the main branch.
    start_release_sync_process "$main_branch" "$release_branch" "$remote_name"
}

# Execute the main function with the provided remote name, main branch, release branch, and operation_mode.
main "$REMOTE_NAME" "$MAIN_BRANCH" "$RELEASE_BRANCH" "$SCRIPT_OPERATION_MODE"
