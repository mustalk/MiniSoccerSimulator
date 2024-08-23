#!/bin/bash
# Copyright 2024 MusTalK (https://github.com/mustalk)
#
# Local Core Utility Script
#
# This script provides core helper functions for local Git operations, including fetching, checking out, and managing branches.
# It also includes functions for ensuring the repository root, setting up Git configuration, and handling commit history differences.
#
# It includes functions for:
#   - ensure_repo_root: Ensures the script is executed from the repository root.
#   - setup_git_config: Sets up Git credentials and user information.
#   - check_remote_exists: Checks if a remote repository exists.
#   - fetch_remote_branch: Fetches the latest changes from a remote branch.
#   - checkout_and_track_branch: Checks out or creates a local branch tracking a remote branch.
#   - create_backup_branch: Creates a backup branch before potentially destructive operations.
#   - get_commit_range: Returns the commit range between two branches.
#   - check_commit_history_diff: Checks for differences in commit history between branches.
#   - get_branch_diffs: Checks and reports differences between branches.
#   - format_commit_messages: Formats commit messages for inclusion in reports.
#   - handle_diff_check_results: Handles the results of a diff check and manages backup branches.
#
# Variables:
#   - FETCH_DEPTH: Controls the depth of fetch operations.
#
# This script is intended to be sourced by other local scripts to provide core Git functionalities and improve code reusability.
#
# Dependencies:
#   - local/common-utils.sh
#   - helpers/format-commits.sh
#   - helpers/check-branch-diffs.sh
#
# Enable strict mode
set -euo pipefail

# Source the common utilities script
source .github/scripts/utils/local/common-utils.sh

# Source the helpers
source .github/scripts/utils/helpers/format-commits.sh
source .github/scripts/utils/helpers/check-branch-diffs.sh

# Configuration for fetch depth
# The fetch depth determines how many commits from the tip of the history are fetched.
# For workflows that need to decide between a rebase & fast-forward or a standard merge,
# it's crucial to fetch more than just the latest commit. Multiple commits might need to be incorporated
# into the main branch, requiring sufficient history to evaluate accurately.
# Using a shallow fetch (e.g., --depth=20) limits the number of commits and improves performance,
# but should be set to a value that ensures all relevant commits are available.
# Adjust the depth based on the complexity of your branching and merging strategy.
FETCH_DEPTH=20

# Function to ensure we're in the root directory of the repository
# This function changes the working directory to the root of the repository.
# If it fails, it triggers a failure, as subsequent commands depend on being in the root directory.
ensure_repo_root() {
    cd "$(git rev-parse --show-toplevel)" || handle_failure "Failed to change directory to the repository root."
}

# Function to set up Git configuration
# This function sets up Git credentials and user information to ensure that Git operations
# that require authentication can prompt for credentials and that commits are made with the correct user info.
setup_git_config() {
    # Set the manager helper to prompt for credentials when needed.
    git config --global credential.helper manager

    # Set Git username and email from global configuration
    git config user.name "$(git config --global user.name)"
    git config user.email "$(git config --global user.email)"
}

# Function to check if the remote repository exists
# This function checks if the specified remote repository exists by verifying the presence of remote branches.
# If the remote repository does not exist, it triggers a failure.
check_remote_exists() {
    local remote_name=$1
    if ! git ls-remote --heads "$remote_name" &>/dev/null; then
        handle_failure "Repository \`$remote_name\` not found. Please check the remote URL and your permissions."
    fi
}

# Function to fetch the latest changes from the remote repository
# This function fetches the latest changes from the specified remote repository with the configured fetch depth.
# If the fetch operation fails, it triggers a failure.
fetch_remote_branch() {
    local remote_name=$1

    # Check if the remote exists
    check_remote_exists "$remote_name"

    # Fetch the latest changes with the specified fetch depth
    git fetch "$remote_name" --depth="$FETCH_DEPTH" || handle_failure "Failed to fetch changes from \`$remote_name\`."
}

# Function to checkout or create a branch from a remote branch
# This function checks out a local branch based on a remote branch. If the branch does not exist locally,
# it creates a new one that tracks the remote branch.
checkout_and_track_branch() {
    local branch=$1
    local remote_name=$2

    # Force checkout the branch and ensure it tracks the remote branch
    if ! git checkout --force -B "$branch" "$remote_name/$branch"; then
        handle_failure "Failed to checkout and update \`$branch\` branch."
    fi
}

# Function to create a backup branch before starting the merge
# This function creates a backup branch from the current branch before starting the merge process.
# The backup branch is named to indicate that it was created before a local rebase.
create_backup_branch(){
    local branch=$1
    local operation_mode=$2

    # Create a backup branch with a timestamped name
    backup_branch="backup/$branch-pre-local-rebase-$operation_mode-$(date +%Y%m%d%H%M)"
    if ! git  branch "$backup_branch" "$branch"; then
        handle_failure "Failed to create backup branch \`$backup_branch\`." "false"
    else
        handle_info "Created backup branch \`$backup_branch\`."
    fi
    echo "$backup_branch"
}

# Function to get the commit range between branches
# This function returns the commit range between the main and release branches
# as a string that can be used in Git commands.
get_commit_range() {
    local main_branch=$1
    local release_branch=$2
    local remote_name=$3

    # Return the commit range for the given branches
    echo "$remote_name/$main_branch..$remote_name/$release_branch"
}

# Function to check for differences in commit history between branches
# This function checks if there are any commits, including merge commits, in the main branch that are not present in the release branch.
# Unlike the 'git log' check for ahead commits, which only considers content differences, this function ensures that no commits are missing
# in the commit history, regardless of whether they introduce changes. Custom fetch --depth needed for this.
# If there are missing commits, the function returns failure (1); otherwise, it returns success (0).
check_commit_history_diff() {
    local main_branch="$1"
    local release_branch="$2"
    local remote_name="$3"

    # Get the list of commits that are in the main branch but not in the release branch.
    local missing_commits
    missing_commits=$(git rev-list --left-only "$remote_name/$main_branch...$remote_name/$release_branch")

    if [[ -n "$missing_commits" ]]; then
        return 1
    else
        return 0
    fi
}

# Function to check branch differences and append the output to the main message
# This function compares the differences between the release and main branches.
# It appends the diff output to the provided message, which can help in investigating conflicts or reviewing changes.
get_branch_diffs() {
    local branch_release=$1
    local branch_main=$2
    local remote_name=$3
    local operation_mode=$4

    # Append the diff check output directly to the main message
    message="$(check_branch_diffs "$remote_name/$branch_release" "$remote_name/$branch_main" "$operation_mode")"

    echo "$message"
}

# Function to format commit messages and append them to the main message
# This function formats a list of commit messages for inclusion in the main message.
# It calls an external script to handle the formatting and appends the formatted output
# to the provided message string.
format_commit_messages() {
    local status_commit_messages="$1"
    local message="$2"
    local repository="$3"

    # Format and append commit messages if they exist
    if [[ -n "$status_commit_messages" ]]; then
        formatted_commits=$(format_commits "$status_commit_messages" "$repository")
        message+="\nThe following commits were included in the merge:\n"
        message+="$formatted_commits"
    fi

    echo "$message"
}

# Function to handle the results of a diff check
# This function determines whether the diff check operation was successful or if a warning was issued.
# If the diff check is successful, it deletes the backup branch.
handle_diff_check_results() {
    local diff_check_output=$1
    local backup_branch=$2
    local diff_check_status=""

    # Determine the success or warning status based on the diff check output
    if echo "$diff_check_output" | grep -q "WARNING"; then
        diff_check_status="warning"
    else
        diff_check_status="success"
    fi

    # Delete the backup branch if the status is success
    if [[ "$diff_check_status" == "success" ]] && [[ -n "$backup_branch" ]]; then
        git branch -D "$backup_branch" && handle_info "Deleted backup branch: $backup_branch"
    fi
}
