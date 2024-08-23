#!/bin/bash
# Copyright 2024 MusTalK (https://github.com/mustalk)
#
# Local Common Utility Script
#
# This script provides a collection of utility functions used by other scripts in the repository
# It includes functions for handling success and failure scenarios, logging, stashing changes, and managing branches.
#
# It includes functions for:
#   - sanitize_message: Sanitizes log messages for consistent formatting.
#   - handle_failure: Handles script failures, logs errors, and optionally restores the stash.
#   - handle_success: Handles script success, logs messages, and optionally restores the stash.
#   - handle_info: Logs informational messages.
#   - handle_hint: Logs hint or tip messages.
#   - handle_warning: Logs warning messages.
#   - abort_rebase_user: Handles rebase aborts due to user interruption.
#   - abort_rebase_error: Handles rebase aborts due to errors.
#   - setup_traps: Sets up traps for user interruptions and errors during rebase.
#   - stash_changes: Stashes uncommitted changes before operations.
#   - pop_stash: Restores stashed changes.
#   - save_starting_point_branch: Saves the current branch as a reference point.
#   - restore_starting_point_branch: Restores the saved starting point branch.
#
# Variables:
#   - starting_branch: Stores the name of the branch active at the start of the script.
#   - STASH_NAME: Stores the name of the stash created by the script.
#
# This script is intended to be sourced by other local scripts to provide common functionality and improve code reusability.
#
# Enable strict mode
set -euo pipefail

# Variable to store the starting point branch
starting_branch=""

# Function to sanitize log messages
# This function removes leading colons and replaces actual newlines with '\n'.
# It ensures that log messages are properly formatted for environments
# that do not handle multiline messages well, such as logging or environment variables.
sanitize_message() {
    # 1st sed expression: Removes leading colons (common in Git command outputs)
    # 2nd sed expression: Replaces actual newlines with '\n' to keep messages single-line.
    echo "$1" | sed -e 's/^:\+//' -e ':a;N;$!ba;s/\n/\\n/g'
}

# Function to handle failure scenarios
# This function is invoked when a failure occurs during script execution.
# It sanitizes and logs the error message, optionally restores the stash,
# aborts any ongoing rebase, and exits the script with a non-zero status code.
handle_failure() {
    local message
    message=$(sanitize_message "$1")
    local error_exit=${2:-"true"}
    local restore_stash=${3:-"$RESTORE_ON_FAILURE"}

    # If a rebase is in progress, abort it
    if git rev-parse -q --verify REBASE_HEAD >/dev/null; then
        echo "Aborting rebase..."
        git rebase --abort || true
    fi

    # Restore the stash if requested
    if [[ "$restore_stash" == "true" ]]; then
        restore_starting_point_branch
        pop_stash
    fi

    # Prefix each non-empty line of the message with "ERROR: "
    echo -e "$message" | sed '/^$/!s/^/ERROR: /' >&2

    # Exit with an error status code if error_exit is true
    if [[ "$error_exit" == "true" ]]; then
        # Remove the traps
        trap - SIGINT ERR
        exit 1
    fi
}

# Function to handle success scenarios
# This function is called upon successful completion of the script.
# It sanitizes and logs the success message, optionally restores the stash,
# switches back to the starting point branch, and exits the script with a zero status code.
handle_success() {
    local message
    message=$(sanitize_message "$1")
    local restore_stash=${2:-"true"}

    # Restore the stash and the starting point branch if requested
    if [[ "$restore_stash" == "true" ]]; then
        restore_starting_point_branch
        pop_stash
    fi

    # Prefix each non-empty line of the message with "SUCCESS: "
    echo -e "$message" | sed '/^$/!s/^/SUCCESS: /' >&2

    # Remove traps and exit successfully
    trap - SIGINT ERR
    exit 0
}

# Function to log informational messages
# This function sanitizes and logs an informational message.
handle_info() {
    local message
    message=$(sanitize_message "$1")

    # Prefix each non-empty line of the message with "INFO: "
    echo -e "$message" | sed '/^$/!s/^/INFO: /' >&2
}

# Function to log hints or tips
# This function sanitizes and logs a hint or tip message.
handle_hint() {
    local message
    message=$(sanitize_message "$1")

    # Prefix each non-empty line of the message with "HINT: "
    echo -e "$message" | sed '/^$/!s/^/HINT: /' >&2
}

# Function to log warning messages
# This function sanitizes and logs a warning message.
handle_warning() {
    local message
    message=$(sanitize_message "$1")

    # Prefix each non-empty line of the message with "WARNING: "
    echo -e "$message" | sed '/^$/!s/^/WARNING: /' >&2
}

# Function to handle aborting a rebase due to user interruption
# This function is triggered when the user interrupts the script (e.g., with Ctrl+C).
# It logs a failure message indicating that the rebase was aborted by the user.
abort_rebase_user() {
    message="Aborting rebase due to user interruption...\n"
    message+="Interactive rebase of \`$RELEASE_BRANCH\` onto \`$MAIN_BRANCH\` was aborted by the user."
    handle_failure "$message"
}

# Function to handle aborting a rebase due to errors
# This function is triggered when an error occurs during the rebase process.
# It logs a failure message indicating that the rebase was aborted due to an error.
abort_rebase_error() {
    message="Aborting rebase due to an error...\n"
    message+="Interactive rebase of \`$RELEASE_BRANCH\` onto \`$MAIN_BRANCH\` failed due to an error."
    handle_failure "$message"
}

# Function to set up traps for user interruptions and errors
# This function sets up traps to handle user interruptions (SIGINT) and errors (ERR).
# It ensures that the rebase process is properly aborted if the script is interrupted or encounters an error.
setup_traps() {
    trap 'abort_rebase_user' SIGINT
    trap 'abort_rebase_error' ERR
}

# Function to stash any uncommitted changes
# This function stashes uncommitted changes to ensure they are not lost during the rebase process.
# The stash is only created if there are uncommitted changes.
stash_changes() {
    local operation_mode=$1
    STASH_NAME="auto-release-$operation_mode-stash-$(date +%s)"
    if ! git diff-index --quiet HEAD --; then
        git stash push -u -m "$STASH_NAME"
        handle_info "Stash created with name $STASH_NAME"
    else
        handle_info "No changes to stash"
    fi
}

# Function to pop the stash
# This function restores the stashed changes, applying them back to the working directory.
# If the specific stash does not exist, it logs an informational message.
pop_stash() {
    if git stash list | grep -q "$STASH_NAME"; then
        handle_info "Un-stashing changes from $STASH_NAME"
        git stash pop "stash@{0}" || handle_info "Failed to pop stash. It might have already been applied."
    else
        handle_info "No specific stash to pop"
    fi
}

# Function to save the current branch as a reference point
# This function saves the current branch name to allow switching back to it after the script completes.
save_starting_point_branch() {
    starting_branch=$(git branch --show-current)
    handle_info "Saved starting point branch name: $starting_branch"
}

# Function to restore the starting point branch after rebase-merge success
# This function switches back to the branch that was active when the script started.
restore_starting_point_branch() {
    git checkout "$starting_branch"
    handle_info "Switched back to the starting point branch: $starting_branch"
}
