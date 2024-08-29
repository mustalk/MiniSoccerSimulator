#!/bin/bash
# Copyright 2024 MusTalK (https://github.com/mustalk)
#
# Local Synchronize Release Script
#
# This script synchronizes the local release branch with the main branch by rebasing and force-pushing.
#
# For more details, see the README.md file in this directory and the DEV_WORKFLOW.md file at the project root.
#
# Enable strict mode
set -euo pipefail

# Set the SCRIPT_OPERATION_MODE variable before sourcing core-utils.sh.
# This variable should be set to "merge" or "sync" depending on the operation mode.
export SCRIPT_OPERATION_MODE="sync"

# Set to "true" to restore the original branch and stash after a failure.
# Set to "false" to stay on the current branch.
export RESTORE_ON_FAILURE="true"

# Source the core utilities script
source .github/scripts/utils/local/core-utils.sh

# Set branch names (modify if needed)
RELEASE_BRANCH="release"
MAIN_BRANCH="main"

# Define remote name (modify if needed)
REMOTE_NAME="origin"

DIFF_EXCLUDE_FILES="CHANGELOG.md|app/build.gradle.kts"

# Set to "true" to enable automatic push after successful rebase sync, "false" to disable
SUCCESS_AUTO_PUSH="false"

# Set to "true" to enable automatic push on conflict situations, "false" to disable
# Might be convenient for specific situations, but be cautious with enabling it.
CONFLICT_AUTO_PUSH="false"

# Variable to store the backup branch name
backup_branch=""

# Function to check if all commits from the release branch are already in the main branch
# This function checks whether the main branch is up-to-date with the release branch.
# If there are no new commits to merge, it logs a success message and exits.
check_commits_up_to_date() {
    local main_branch="$1"
    local release_branch="$2"
    local remote_name="$3"
    local operation_mode="$4"
    local ahead_commits=""
    local commit_range=""

    # Get the commit range between the main and release branches
    commit_range=$(get_commit_range "$main_branch" "$release_branch" "$remote_name")

    # Get the list of commits ahead in the release branch
    ahead_commits="$(git log "$commit_range" --oneline)"

    # If no commits are ahead, the branches are already up-to-date
    if [[ -z "$ahead_commits" ]]; then
        # Check for commit history differences
        if check_commit_history_diff "$main_branch" "$release_branch" "$remote_name"; then
            handle_success "No $operation_mode needed, \`$remote_name/$release_branch\` is already up-to-date with \`$remote_name/$main_branch\`." false
        else
            handle_info "Commit history differs between \`$remote_name/$release_branch\` and \`$remote_name/$main_branch\`. A $operation_mode is required."
        fi
    else
        handle_info "Differences in file contents between \`$remote_name/$release_branch\` and \`$remote_name/$main_branch\`. A $operation_mode is required."
    fi
}

# Function to reset main to release state
# This function resets the main branch to match the state of the release branch.
# If auto-push is enabled, the changes will be force-pushed to the remote repository.
reset_main_to_release() {
    local auto_push="$1"
    git checkout "$MAIN_BRANCH"
    git reset --hard "$RELEASE_BRANCH"
    if [[ "$auto_push" == "true" ]]; then
        git push $REMOTE_NAME $MAIN_BRANCH --force-with-lease
        git checkout "$RELEASE_BRANCH"
        handle_info "\`$MAIN_BRANCH\` branch has been reset to match \`$RELEASE_BRANCH\` state."
    else
        handle_info "\`$MAIN_BRANCH\` branch has been reset to match \`$RELEASE_BRANCH\` state.\n"
        read -p "Force push to remote? (y/N) " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            git push $REMOTE_NAME $MAIN_BRANCH --force-with-lease
            git checkout "$RELEASE_BRANCH"
        else
            handle_failure "Force push aborted." "true"
        fi
    fi
}

# Function to display the resolution options and handle user input
# This function presents the user with options to resolve conflicts that arise
# when synchronizing the release branch with the main branch.
resolve_conflict_options() {
    local diff_message="$1"
    local main_branch="$2"
    local release_branch="$3"
    local conflict_auto_push="$4"

    echo -e "$diff_message"
    echo -e "\nHow would you like to resolve this:"
    echo -e "1) Abort and resolve manually"
    echo -e "2) !!!CAUTION!!! Reset \`$main_branch\` to match \`$release_branch\` state \nThis will discard any commits/changes that aren't present on \`$release_branch\`"
    echo -n "Choose an option (1-2): "

    read -r choice
    case $choice in
        1)
            handle_failure "Sync aborted.\nPlease resolve manually." "true"
            ;;
        2)
            echo -e "\n!!!WARNING!!!: Resetting \`$main_branch\` will overwrite any changes that are not present in \`$release_branch\`!\n"
            echo -e "Proceed carefully, and make sure you really want to discard the \`$main_branch\` branch changes."
            read -p "Are you sure you want to reset main? (y/N) " -n 1 -r
            echo
            if [[ $REPLY =~ ^[Yy]$ ]]; then
                reset_main_to_release "$conflict_auto_push"
            else
                handle_failure "Reset aborted." "true"
            fi
            ;;
        *)
            handle_failure "Invalid option selected. Aborting." "true"
            ;;
    esac
}

# Function to handle differences between the release and main branches
# and decide on the appropriate actions based on the detected differences.
handle_branch_diffs() {
    local release_branch="$1"
    local main_branch="$2"
    local remote_name="$3"
    local operation_mode="$4"
    local diff_excluded_files="$5"
    local diff_message=""

    # Un-stash changes (so that the check-branch-diffs.sh permission is restored to run the script)
    # Required if .git/config filemode isn't set to 'false', default is 'true'
    if [[ "$(git config core.filemode)" == "true" ]]; then
        pop_stash
    fi

    # As a safety net, check for differences between the release and main branches,
    # and capture the output in the diff_message variable.
    diff_message=$(get_branch_diffs "$release_branch" "$main_branch" "$remote_name" "$operation_mode" "$diff_excluded_files")

    # Based on the content of diff_message, decide on the appropriate course of action.
    # If the message contains "INFO:", log it as an informational message.
    # If the message contains "WARNING:", treat it as a failure and exit.
    if [[ -n "$diff_message" ]]; then
        if echo "$diff_message" | grep -q "INFO:"; then
            handle_info "$diff_message"
        elif echo "$diff_message" | grep -q "WARNING:"; then
            resolve_conflict_options "$diff_message" "$main_branch" "$release_branch" "$CONFLICT_AUTO_PUSH"
        fi
    fi

    # Stash changes again before the rebase to avoid any potential issues
    if [[ "$(git config core.filemode)" == "true" ]]; then
        stash_changes
    fi

    # Handle the diff check results and possibly delete the backup branch
    handle_diff_check_results "$diff_message" "$backup_branch"
}

# Function to push the rebased release branch to the remote repository.
# The changes are force-pushed to ensure that the remote branch is updated
# with the rebased history from the main branch.
push_release_branch() {
    local main_branch="$1"
    local release_branch="$2"
    local remote_name="$3"

    # Force push the rebased release branch to the remote repository using the --force-with-lease option.
    # If the push is successful, trigger the success handler.
    # If the push fails, trigger a failure handler to log the error and exit.
    if git push "$remote_name" "$release_branch" --force-with-lease; then
        handle_hint "To enable automatic push, set SUCCESS_AUTO_PUSH to 'true'"
        handle_success "Successfully synchronized the \`$release_branch\` branch with \`$main_branch\`."
    else
        handle_failure "Failed to push changes to the \`$release_branch\` branch. Please check and resolve manually."
    fi
}

# Function to synchronize the release branch with the main branch by performing a rebase.
# This function attempts to rebase the release branch onto the main branch.
start_release_sync_process() {
    local main_branch="$1"
    local release_branch="$2"
    local remote_name="$3"

    # Attempt to rebase the release branch onto the main branch.
    # If the rebase is successful, proceed to push the changes.
    # If the rebase fails, trigger a failure handler to log the error and exit.
    if git rebase "$main_branch"; then
        # Check if SUCCESS_AUTO_PUSH is set to "true"
        if [[ "$SUCCESS_AUTO_PUSH" == "true" ]]; then
            push_release_branch "$main_branch" "$release_branch" "$remote_name"
        else
            # Prompt the user to confirm the force push
            handle_warning "Please review the rebase changes before proceeding."
            read -p "Force push changes to the \`$release_branch\` branch? (y/N) " -n 1 -r
            echo
            # Check if response is either "Y" or "y".
            if [[ $REPLY =~ ^[Yy]$ ]]; then
                push_release_branch "$main_branch" "$release_branch" "$remote_name"
            else
                handle_info "Force push aborted."
            fi
        fi
    else
        handle_failure "Failed to rebase \`$release_branch\` branch onto \`$main_branch\`. Please check and resolve manually."
    fi
}

# Main function to execute the sync process.
# This is the entry point of the script.
main() {
    local remote_name="$1"
    local main_branch="$2"
    local release_branch="$3"
    local operation_mode="$4"
    local diff_excluded_files="$5"

    # Ensure that the script is running in the root directory of the repository.
    ensure_repo_root

    # Set up traps for error handling
    setup_traps

    # Configure Git settings as needed for the script.
    setup_git_config

    # Fetch the latest changes from the remote repository.
    fetch_remote_branch "$remote_name"

    # Check if the release branch has any new commits that need to be merged into the main branch.
    # If the main branch is already up-to-date, the script will exit early with a success message.
    check_commits_up_to_date "$main_branch" "$release_branch" "$remote_name" "$operation_mode"

    # Save the current branch as a reference point to return to after the script completes.
    save_starting_point_branch

    # Stash uncommitted changes.
    stash_changes "$operation_mode"

    # Checkout the main branch and ensure it tracks the remote branch.
    checkout_and_track_branch "$main_branch" "$remote_name"

    # Checkout the release branch and ensure it tracks the remote branch.
    checkout_and_track_branch "$release_branch" "$remote_name"

    # Create a backup branch before proceeding with the merge.
    backup_branch=$(create_backup_branch "$main_branch" "$operation_mode")

    # Handle any differences found between the branches.
    handle_branch_diffs "$release_branch" "$main_branch" "$remote_name" "$operation_mode" "$diff_excluded_files"

    # Log an informational message indicating the start of the sync process.
    handle_info "Starting $operation_mode of \`$release_branch\` with \`$main_branch\` ..."

    # Start the process to synchronize the release branch with the main branch.
    start_release_sync_process "$main_branch" "$release_branch" "$remote_name"
}

# Execute the main function with the provided remote name, main branch, release branch, and operation_mode.
main "$REMOTE_NAME" "$MAIN_BRANCH" "$RELEASE_BRANCH" "$SCRIPT_OPERATION_MODE" "$DIFF_EXCLUDE_FILES"
