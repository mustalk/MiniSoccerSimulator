#!/bin/bash
# Copyright 2024 MusTalK (https://github.com/mustalk)
#
# Auto Rebase and Merge Script
#
# This script automates the rebasing and merging of the release branch onto the main branch within a CI/CD pipeline..
#
# For more detailed documentation and important considerations, see the README.md file in this directory.
#
# Enable strict mode
set -euo pipefail

# Set the SCRIPT_OPERATION_MODE variable before sourcing core-utils.sh.
# This variable should be set to "merge" or "sync" depending on the operation mode.
export SCRIPT_OPERATION_MODE="merge"

# Enable / Disable debug mode for the logs.
export DEBUG="false"

# Source the core utilities script
source .github/scripts/utils/auto/core-utils.sh

# List of required environment variables.
REQUIRED_VARS=(GITHUB_REPOSITORY RELEASE_BRANCH MAIN_BRANCH REMOTE_NAME DIFF_EXCLUDE_FILES)

# Function to check if all commits from release are already in main.
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
# This function handles post-merge operations, including pushing the merged branch.
# to the remote repository, formatting commit messages, checking for any remaining differences
# between branches, and logging the final success message.
handle_successful_merge() {
    local status_commit_messages="$1"
    local branch_release="$2"
    local branch_main="$3"
    local remote_name="$4"
    local operation_mode="$5"
    local diff_excluded_files="$6"
    local ff_merge=${7:-"true"}

    # Push the updated main branch to the remote repository.
    run_cmd git push "$remote_name" "$branch_main" || handle_failure "Error: Failed to push changes to \`$remote_name/$branch_main\`."

    # Store the success message.
    if [[ "$ff_merge" == "true" ]]; then
        local message="Rebase and Fast-Forward merge of \`$branch_release\` into \`$branch_main\` completed successfully."
    else
        local message="Standard merge of \`$branch_release\` into \`$branch_main\` completed successfully."
    fi

    # Restore execute permissions to the scripts before running them.
    restore_script_permissions

    # Format the merged commit messages and append them to the status message.
    message="$(format_commit_messages "$status_commit_messages" "$message" "$GITHUB_REPOSITORY")"

    # Check for unmerged commits and/or file content differences.
    # on main and append the output to the status message to make investigating conflicts easier.
    message+="$(get_branch_diffs "$branch_release" "$branch_main" "$remote_name" "$operation_mode" "$diff_excluded_files")"

    # We don't restore perms here as we did it a few steps earlier already.
    handle_success "$message" "false"
}

# Function to handle a single commit merge.
# This function performs a squash merge when there is only one commit in the release branch.
# It handles deleted and renamed files and creates a new commit with the last commit message from the release branch.
merge_single_commit() {
    local branch_release="$1"
    local branch_main="$2"
    local deleted_files="$3"
    local renamed_files="$4"

    # Local variables
    local last_commit
    local last_commit_msg
    local last_commit_hash
    local last_commit_title
    local author_date_options

    # Get the last commit message.
    last_commit="$(git log -1 --pretty=format:'%H %h %s' "$branch_release")"
    # Extract commit body.
    last_commit_msg="$(git log -1 --pretty=format:'%B' "$branch_release")"
    # Extract commit hash.
    last_commit_hash="$(echo "$last_commit" | cut -d' ' -f1)"
    # Extract commit title.
    last_commit_title="$(echo "$last_commit" | cut -d' ' -f3-)"

    # Extract commit author info and date from the last commit.
    author_date_options=$(extract_author_and_date_info "$last_commit_hash")

    # Check if the commit message already exists in main.
    if run_cmd check_commit_message_exists "$last_commit_msg" "$branch_main"; then
        last_commit_msg="Merge '$branch_release'\n$last_commit_msg"
    fi

    # Perform a merge with -X theirs strategy favoring release and create a squash commit with the last release commit message.
    if run_cmd git merge -X theirs --squash --stat "$branch_release"; then

        # Handle deleted and renamed files that didn't reflect the correct state after the merge.
        handle_deleted_and_renamed_files "$deleted_files" "$renamed_files"

        # Create a new commit with the last commit message from release, using a temporary file, to preserve the commit formatting.
        commit_with_temp_file "$last_commit_msg" "$author_date_options"

        handle_info "Squash merged single commit from \`$branch_release\` into \`$branch_main\`."

        # Update commit messages merged for logs.
        echo "$last_commit"
    else
        # Abort merge if in progress.
        if run_cmd git rev-parse -q --verify MERGE_HEAD >/dev/null; then
            run_cmd git merge --abort || true
        fi
        failure_message="The single squash commit merge of \`$branch_release\` into \`$branch_main\` failed! Please resolve manually.\n"
        failure_message+="Attempted to merge:\n* $last_commit_title"
        handle_failure "$failure_message"
    fi
}

# Function to handle multiple commits merge.
# This function performs a standard merge when there are multiple commits in the release branch.
# It handles deleted and renamed files during the merge process and commits the changes with an appropriate message.
merge_multiple_commits() {
    local branch_release="$1"
    local branch_main="$2"
    local deleted_files="$3"
    local renamed_files="$4"

    # Gather commit messages and capture both merge commit and status commit messages into an array.
    mapfile -t commit_messages < <(gather_commit_messages "$branch_release" "$branch_main")

    # Assign array elements to the variables.
    merge_msg="${commit_messages[0]}"
    status_msg="${commit_messages[1]}"

    # Perform a standard merge with the gathered commit messages.
    handle_info "Starting a standard merge for branch \`$branch_release\` into \`$branch_main\`."
    if run_cmd git merge -X theirs --no-ff --stat --no-commit "$branch_release"; then

        # Handle deleted and renamed files that didn't reflect the correct state after the merge.
        handle_deleted_and_renamed_files "$deleted_files" "$renamed_files"

        # Commit the merge with the gathered commit message using a temporary file, to preserve the commit formatting.
        commit_with_temp_file "$merge_msg"

        handle_info "Successfully merged multiple commits from \`$branch_release\` into \`$branch_main\`."

        # Update commit messages merged for logs.
        echo "$status_msg"
    else
        # Abort merge if in progress.
        if run_cmd git rev-parse -q --verify MERGE_HEAD >/dev/null; then
            run_cmd git merge --abort || true
        fi
        failure_message="The multiple commits merge of \`$branch_release\` into \`$branch_main\` failed! Please resolve manually.\n"
        failure_message+="Attempted to merge:\n$merge_msg"
        handle_failure "$failure_message"
    fi
}

# Function to start the standard merge process.
# Depending on the number of commits in the release branch, this function will handle
# either a single commit or multiple commits during the merge process.
start_standard_merge_process() {
    local branch_release="$1"
    local branch_main="$2"
    local deleted_files="$3"
    local renamed_files="$4"

    # Initialize variable to capture the success commit messages.
    local status_commit_messages=""

    # Debugging the difference between the main and release branches.
    run_cmd git diff --name-status "$branch_main" "$branch_release"

    # Determine the number of commits in the release branch that are not in the main branch.
    if check_multiple_commits "$branch_release" "$branch_main"; then
        # Handle multiple commits merge.
        status_commit_messages=$(merge_multiple_commits "$branch_release" "$branch_main" "$deleted_files" "$renamed_files")
    else
        # Handle single commit merge.
        status_commit_messages=$(merge_single_commit "$branch_release" "$branch_main" "$deleted_files" "$renamed_files")
    fi

    echo "$status_commit_messages"
}

# Function to perform a standard merge when there are deleted files.
# This function ensures that the main branch is checked out before merging
# and uses the `start_standard_merge_process` function to handle the merge.
perform_standard_merge() {
    local release_branch="$1"
    local main_branch="$2"
    local remote_name="$3"
    local operation_mode="$4"
    local diff_excluded_files="$5"
    local deleted_files="$6"
    local renamed_files="$7"
    local status_commit_messages=""

    handle_info "Starting standard merge process..."

    # Ensure we are on the main branch before the merge process.
    run_cmd git checkout "$main_branch"

    # Start the standard merge process, passing the deleted and renamed files for handling.
    status_commit_messages=$(start_standard_merge_process "$release_branch" "$main_branch" "$deleted_files" "$renamed_files")

    # Handle the successful merge with appropriate status messages.
    handle_successful_merge "$status_commit_messages" "$release_branch" "$main_branch" "$remote_name" "$operation_mode" "$diff_excluded_files" "false"
}

# Function to handle the rebase and fast-forward merge process.
# This function checks out the release branch, performs a rebase onto the main branch,
# and then attempts a fast-forward merge. It handles the outcome of the operation.
perform_rebase_and_ff_merge() {
    local release_branch="$1"
    local main_branch="$2"
    local remote_name="$3"
    local operation_mode="$4"
    local diff_excluded_files="$5"
    local commit_range=""
    local status_commit_messages=""

    # Ensure we are on the release branch before rebase.
    run_cmd git checkout "$release_branch"

    # Get the range of commits that will be covered by the rebase.
    commit_range=$(get_commit_range "$main_branch" "$release_branch" "$remote_name")
    status_commit_messages="$(git log --pretty="%H %h %s" "$commit_range")"

    # Rebase using the default ort strategy with -X theirs for conflict resolutions, favoring the release branch changes.
    # Refer to the README.md in this directory for more details about the reasoning behind using this strategy.
    if run_cmd git rebase -X theirs "$main_branch"; then
        handle_info "Successfully rebased \`$release_branch\` onto \`$main_branch\`."
        run_cmd git checkout "$main_branch"
        if run_cmd git merge --ff-only "$release_branch"; then
            # Handle the successful merge with appropriate status messages.
            handle_successful_merge "$status_commit_messages" "$release_branch" "$main_branch" "$remote_name" "$operation_mode" "$diff_excluded_files" "true"
        else
            handle_failure "The fast-forward merge of \`$release_branch\` into \`$main_branch\` failed! Please resolve manually."
        fi
    else
        handle_failure "The rebase of \`$release_branch\` onto \`$main_branch\` failed! Please resolve manually."
    fi
}

# Function to handle informational logs and return whether to perform a standard merge or rebase.
# This function checks for deleted files, renamed files, or multiple commits in the release branch.
# It determines whether to proceed with a standard merge or a rebase and fast-forward merge based on these conditions.
evaluate_merge_strategy() {
    local deleted_files="$1"
    local renamed_files="$2"
    local release_branch="$3"
    local main_branch="$4"

    # Check for multiple commits or deleted/renamed files.
    if [[ -n "$deleted_files" ]] || [[ -n "$renamed_files" ]] || check_multiple_commits "$release_branch" "$main_branch"; then
        if [[ -n "$deleted_files" && -n "$renamed_files" ]]; then
            handle_info "There are both deleted and renamed files in \`$release_branch\`:\nDeleted:\n$deleted_files\nRenamed:\n$renamed_files"
            handle_info "Performing standard merge instead of rebase."
        elif [[ -n "$deleted_files" ]]; then
            handle_info "There are deleted files in \`$release_branch\` but not in \`$main_branch\`:\n$deleted_files"
            handle_info "Performing standard merge instead of rebase."
        elif [[ -n "$renamed_files" ]]; then
            handle_info "There are renamed files in \`$release_branch\` but not in \`$main_branch\`:\n$renamed_files"
            handle_info "Performing standard merge instead of rebase."
        else
            handle_info "Multiple commits detected in \`$release_branch\` but not in \`$main_branch\`.\nPerforming standard merge instead of rebase."
        fi
        # Indicate that a standard merge should be performed.
        return 0
    fi
    # Indicate that a rebase and fast-forward merge should be performed.
    return 1
}

# Function to start the rebase and merge process.
# This function checks for deleted files and multiple commits between the release and main branches.
# If deleted files are found or multiple commits exist, it triggers a standard merge,
# otherwise, it performs a rebase and fast-forward merge.
start_rebase_merge_process() {
    local release_branch="$1"
    local main_branch="$2"
    local remote_name="$3"
    local operation_mode="$4"
    local diff_excluded_files="$5"
    local deleted_files
    local renamed_files

    # Check for deleted files in the release branch compared to the main branch.
    deleted_files=$(get_deleted_files "$main_branch" "$release_branch")

    # Check for renamed files in the release branch compared to the main branch.
    renamed_files=$(get_renamed_files "$main_branch" "$release_branch")

    # Decide whether to perform standard merge or rebase.
    if evaluate_merge_strategy "$deleted_files" "$renamed_files" "$release_branch" "$main_branch"; then
        perform_standard_merge "$release_branch" "$main_branch" "$remote_name" "$operation_mode" "$diff_excluded_files" "$deleted_files" "$renamed_files"
    else
        # Perform a rebase and fast-forward merge if no deleted/renamed files and only one commit.
        perform_rebase_and_ff_merge "$release_branch" "$main_branch" "$remote_name" "$operation_mode" "$diff_excluded_files"
    fi
}

# Main function to execute the rebase and merge process.
# This is the entry point of the script.
main() {
    # Check if all required environment variables are set.
    # This prevents the script from running if crucial variables are missing.
    check_required_vars "${REQUIRED_VARS[@]}"

    # Capture script variables
    local remote_name="$1"
    local main_branch="$2"
    local release_branch="$3"
    local operation_mode="$4"
    local diff_excluded_files="$5"

    # Set up logging for the script.
    # This ensures that all relevant output is captured in a log file.
    setup_logging

    # Ensure that the script is running in the root directory of the repository.
    # This is necessary to avoid path issues during Git operations.
    ensure_repo_root

    # Ensure that the GPG agent is running for signing commits.
    # This is required to sign commits.
    ensure_gpg_agent

    # Fetch the latest changes from the remote repository.
    # This ensures that the local branches are up-to-date with the remote.
    fetch_remote_branch "$remote_name"

    # Check if the release branch has any new commits that need to be merged into the main branch.
    # If the main branch is already up-to-date, the script will exit early with a success message.
    check_commits_up_to_date "$main_branch" "$release_branch" "$remote_name" "$operation_mode"

    # Log an informational message indicating the start of the rebase and merge process.
    handle_info "Starting $operation_mode of \`$release_branch\` into \`$main_branch\` ..."

    # Ensure that the working directory is clean before proceeding.
    # This prevents conflicts caused by untracked files or changes in the working directory.
    ensure_clean_working_directory

    # Check out the main branch and ensure it is tracking the remote branch.
    checkout_and_track_branch "$main_branch" "$remote_name"

    # Check out the release branch and ensure it is tracking the remote branch.
    checkout_and_track_branch "$release_branch" "$remote_name"

    # Start the rebase and merge process.
    # This is the core operation where the release branch is rebased onto the main branch,
    # and the result is either fast-forwarded or merged depending on the situation.
    start_rebase_merge_process "$release_branch" "$main_branch" "$remote_name" "$operation_mode" "$diff_excluded_files"
}

# Execute the main function with the provided environment variables.
main "$REMOTE_NAME" "$MAIN_BRANCH" "$RELEASE_BRANCH" "$SCRIPT_OPERATION_MODE" "$DIFF_EXCLUDE_FILES"
