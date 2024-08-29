#!/bin/bash
# Copyright 2024 MusTalK (https://github.com/mustalk)
#
# Local Interactive Rebase and Merge Script
#
# This script provides an interactive way to rebase and merge a release branch onto the main branch locally.
#
# See the README.md in this directory for detailed usage instructions and information.
#
# Enable strict mode
set -euo pipefail

# Set the SCRIPT_OPERATION_MODE variable before sourcing core-utils.sh.
# This variable should be set to "merge" or "sync" depending on the operation mode.
export SCRIPT_OPERATION_MODE="merge"

# Set the GitHub repository (used to format commit messages).
export GITHUB_REPOSITORY="mustalk/MiniSoccerSimulator"

# Set to "true" to restore the original branch and stash after a failure.
# Set to "false" to stay on the current branch.
export RESTORE_ON_FAILURE="false"

# Source the core utilities script.
source .github/scripts/utils/local/core-utils.sh

# Define branch names (modify if needed).
RELEASE_BRANCH="release"
MAIN_BRANCH="main"

# Define remote name (modify if needed).
REMOTE_NAME="origin"

# Set to "true" to enable automatic push after successfully rebase-merging, "false" to disable.
SUCCESS_AUTO_PUSH="false"

# Set to "true" to enable automatic push after resolving conflicts, "false" to disable.
CONFLICT_AUTO_PUSH="false"

# Files excluded from the diff checks
DIFF_EXCLUDE_FILES="CHANGELOG.md|app/build.gradle.kts"

# Variable to store the backup branch name.
backup_branch=""

# Function to check if all commits from the release branch are already in the main branch.
# This function checks whether the main branch is up-to-date with the release branch.
# If there are no new commits to merge, it logs a success message and exits.
check_commits_up_to_date() {
    local main_branch="$1"
    local release_branch="$2"
    local remote_name="$3"
    local operation_mode="$4"
    local ahead_commits=""
    local commit_range=""

    # Get the commit range between the main and release branches.
    commit_range=$(get_commit_range "$main_branch" "$release_branch" "$remote_name")

    # Get the list of commits ahead in the release branch.
    ahead_commits="$(git log "$commit_range" --oneline)"

    # If no commits are ahead, the branches are already up-to-date.
    if [[ -z "$ahead_commits" ]]; then
        handle_success "No $operation_mode needed, \`$remote_name/$main_branch\` is already up-to-date with \`$remote_name/$release_branch\`." "false"
    fi
}

# Function to handle the success case after a successful merge.
# This function manages post-merge operations, including restoring permissions,
# formatting commit messages, and running a diff check between branches.
handle_successful_merge() {
    local message="$1"
    local release_branch="$2"
    local main_branch="$3"
    local remote_name="$4"
    local operation_mode="$5"
    local diff_excluded_files="$6"

    # Restore file permissions if filemode is enabled.
    if [[ "$(git config core.filemode)" == "true" ]]; then
        # Un-stash to restore script permissions.
        pop_stash
    fi

    # Get the commit range between the main and release branches.
    commit_range=$(get_commit_range "$main_branch" "$release_branch" "$remote_name")

    # Get commit messages to be included in the merge.
    commit_messages=$(git log --pretty="%H %h %s" "$commit_range")

    # Format the merged commit messages and append them to the status message.
    message="$(format_commit_messages "$commit_messages" "$message" "$GITHUB_REPOSITORY")"

    # Check for unmerged commits and/or file content differences on the main branch,
    # and append the output to the status message to make investigating conflicts easier.
    diff_check_output="$(get_branch_diffs "$release_branch" "$main_branch" "$remote_name" "$operation_mode" "$diff_excluded_files")"

    # Append the diff check output to the message.
    message="$diff_check_output"

    # Handle the diff check results and possibly delete the backup branch.
    handle_diff_check_results "$diff_check_output" "$backup_branch"

    # Re-stash changes if filemode is enabled.
    if [[ "$(git config core.filemode)" == "true" ]]; then
        stash_changes "$operation_mode"
    fi

    # Log the final success message.
    handle_success "$message"
}

# Function to push changes to the remote repository.
# This function pushes the specified branch to the given remote repository.
# If the push operation is successful, an informational message is logged.
# If the push fails, Git will handle the error by default and the script will stop due to strict mode.
git_push_changes() {
    local main_branch="$1"
    local remote_name="$2"
    # Push the main branch to the remote repository and log a success message.
    git push "$remote_name" "$main_branch" && handle_info "Successfully pushed changes to \`$remote_name/$main_branch\`"
}

# Function to perform the post-rebase merge process.
# This function attempts a fast-forward merge of the main branch with the release branch.
# If the merge is successful, it handles automatic pushing if enabled.
perform_post_rebase_merge() {
    local release_branch="$1"
    local main_branch="$2"
    local remote_name="$3"
    local operation_mode="$4"
    local diff_excluded_files="$5"
    local auto_push="$6"
    local conflict="$7"
    local diff_check_output=""

    # Checkout the main branch.
    git checkout "$main_branch"

    # Attempt a fast-forward merge.
    if git merge --ff-only "$release_branch"; then
        handle_info "Successfully merged \`$release_branch\` into \`$main_branch\`"

        # Check if automatic push is enabled.
        if [[ "$auto_push" == "true" ]]; then
            git_push_changes "$main_branch" "$remote_name"
        else
            handle_hint "To enable automatic push, set SUCCESS_AUTO_PUSH to 'true'"
            # Prompt the user to confirm the push.
            handle_warning "Please review the rebase changes before proceeding."
            read -p "Push changes to the \`$main_branch\` branch? (y/N) " -n 1 -r
            echo
            # Check if response is either "Y" or "y".
            if [[ $REPLY =~ ^[Yy]$ ]]; then
                git_push_changes "$main_branch" "$remote_name"
            else
                handle_info "Push aborted."
            fi
        fi

        # Determine the appropriate success message based on whether conflicts were resolved.
        if [[ "$conflict" == "true" ]]; then
            message="Interactive rebase and merge of \`$release_branch\` onto \`$main_branch\` completed successfully after conflict resolution."
        else
            message="Interactive rebase and merge of \`$release_branch\` onto \`$main_branch\` completed successfully."
        fi

        # Handle the successful merge with appropriate status messages.
        handle_successful_merge "$message" "$release_branch" "$main_branch" "$remote_name" "$operation_mode" "$diff_excluded_files"
    else
        # Handle the failure of the fast-forward merge.
        if [[ "$conflict" == "true" ]]; then
            message="Interactive rebase of \`$release_branch\` onto \`$main_branch\` failed."
            message+="Fast-forward merge failed. Please ensure the main branch is up-to-date."
        else
            message="Rebase completed successfully. However, the fast-forward merge failed."
            message+="Please ensure the main branch is up-to-date and try merging again."
        fi
        handle_failure "$message"
    fi
}

# Function to start the rebase and merge process.
# This function initiates an interactive rebase of the release branch onto the main branch.
# If the rebase is successful, it attempts a fast-forward merge.
# If conflicts are encountered, the user is prompted to resolve them manually.
start_rebase_merge_process() {
    local release_branch="$1"
    local main_branch="$2"
    local remote_name="$3"
    local operation_mode="$4"
    local diff_excluded_files="$5"

    # Rebase the release branch onto the main branch interactively.
    if git rebase -i "$main_branch"; then
        handle_info "Rebase completed successfully."

        # Create a backup branch before proceeding with the merge.
        backup_branch=$(create_backup_branch "$main_branch" "$operation_mode")

        # Attempt a fast-forward merge.
        perform_post_rebase_merge "$release_branch" "$main_branch" "$remote_name" "$operation_mode" "$diff_excluded_files" "$SUCCESS_AUTO_PUSH" "false"
    else
        handle_warning "Resolve conflicts manually and continue the rebase."

        # Prompt the user to resolve conflicts and continue the rebase.
        while ! git rebase --continue; do
            handle_warning "Rebase conflict resolution in progress. Please resolve conflicts and continue the rebase."
            read -r -p "Press Enter to continue after resolving conflicts..."
        done

        # Attempt a fast-forward merge again after conflicts resolution.
        perform_post_rebase_merge "$release_branch" "$main_branch" "$remote_name" "$operation_mode" "$diff_excluded_files" "$CONFLICT_AUTO_PUSH" "true"
    fi
}

# Main function to execute the rebase and merge process.
# This is the entry point of the script.
main() {
    local remote_name="$1"
    local main_branch="$2"
    local release_branch="$3"
    local operation_mode="$4"
    local diff_excluded_files="$5"

    # Ensure that the script is running in the root directory of the repository.
    ensure_repo_root

    # Set up traps for error handling.
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

    # Log an informational message indicating the start of the rebase and merge process.
    handle_info "Starting interactive rebase of $release_branch onto $main_branch ..."

    # Stash any uncommitted changes before switching branches.
    stash_changes "$operation_mode"

    # Checkout the main branch and ensure it tracks the remote branch.
    checkout_and_track_branch "$main_branch" "$remote_name"

    # Checkout the release branch and ensure it tracks the remote branch.
    checkout_and_track_branch "$release_branch" "$remote_name"

    # Start the rebase and merge process.
    start_rebase_merge_process "$release_branch" "$main_branch" "$remote_name" "$operation_mode" "$diff_excluded_files"
}

# Execute the main function with the provided remote name, main branch, release branch, and operation_mode.
main "$REMOTE_NAME" "$MAIN_BRANCH" "$RELEASE_BRANCH" "$SCRIPT_OPERATION_MODE" "$DIFF_EXCLUDE_FILES"
