#!/bin/bash
# Copyright 2024 MusTalK (https://github.com/mustalk)
#
# Core Utility Script
#
# This script provides core utility functions for Git operations and other script functionalities.
#
# It includes functions for:
# - Ensuring the script is running in the repository root and the GPG agent is running
# - Checking for the existence of remote repositories
# - Fetching changes from remote repositories
# - Ensuring a clean working directory
# - Checking out and tracking branches
# - Getting the commit range between branches
# - Identifying deleted and renamed files between branches
# - Checking for differences in commit history between branches
# - Checking for the existence of specific commit messages in a branch
# - Checking for multiple commits to be merged in a branch
# - Extracting PR numbers from commit titles
# - Gathering and formatting commit messages
# - Handling deleted and renamed files during merges
# - Committing changes with temporary commit message files
#
# This script is intended to be sourced by other scripts and should not be executed directly.
#
# Environment Variables:
#   - FETCH_DEPTH: Controls the depth of fetch operations (default: 20).
#
# Dependencies:
#   - auto/common-utils.sh
#   - helpers/release/format-commits.sh
#   - helpers/release/check-branch-diffs.sh
#
# Enable strict mode
set -euo pipefail

# Source the common utilities script
source .github/scripts/utils/auto/common-utils.sh

# Source the helpers
source .github/scripts/utils/helpers/release/format-commits.sh
source .github/scripts/utils/helpers/release/check-branch-diffs.sh

# Configuration for fetch depth
# The fetch depth determines how many commits from the tip of the history are fetched.
# For workflows that need to decide between a rebase&fast-forward or a standard merge, like we are doing on this script
# it's crucial to fetch more than just the latest commit. This is because multiple commits  might need to be incorporated
# into the main branch, which requires fetching sufficient history to evaluate accurately.
# Using a shallow fetch (e.g., --depth=20) limits the number of commits and improves performance,
# but should be set to a value that ensures all relevant commits are available.
# Adjust the depth based on the complexity of your branching and merging strategy.
FETCH_DEPTH=20

# Function to ensure we're in the root directory of the repository
# This function is precautionary and changes the working directory to the root of the repository.
# If it fails to do so, it triggers a failure.
ensure_repo_root() {
    cd "$(git rev-parse --show-toplevel)" || handle_failure "Failed to change directory to the repository root." "false"
}

# Function to ensure the GPG agent is running
# This function ensures that the GPG agent is running by launching it if necessary.
ensure_gpg_agent() {
    run_cmd gpgconf --launch gpg-agent
}

# Function to check if the remote repository exists
# This function checks if a given remote repository exists. If it doesn't,
# it triggers a failure.
check_remote_exists() {
    local remote_name="$1"
    if ! git ls-remote --heads "$remote_name" &>/dev/null; then
        handle_failure "Repository \`$remote_name\` not found. Please check the remote URL and your permissions." "false"
    fi
}

# Function to fetch the latest changes from the remote repository
# This function fetches the latest changes from the specified remote repository.
# If the fetch operation fails, it triggers a failure.
fetch_remote_branch() {
    local remote_name="$1"

    # Check if the remote exists
    check_remote_exists "$remote_name"

    run_cmd git fetch "$remote_name" --depth="$FETCH_DEPTH" || handle_failure "Failed to fetch changes from \`$remote_name\`." "false"
}

# Function to ensure a clean working directory
# This function ensures that the working directory is clean by removing untracked files
# and resetting the working directory to match the latest commit.
# on this scenario its mainly script permission changes, that would interfere in the next process steps
ensure_clean_working_directory() {
    run_cmd git clean -fd --dry-run
    run_cmd git reset --hard HEAD
    run_cmd git clean -fd
}

# Function to checkout or create a branch from a remote branch
# This function checks out a local branch based on a remote branch. If the branch doesn't exist locally,
# it creates a new one that tracks the remote branch.
checkout_and_track_branch() {
    local branch="$1"
    local remote_name="$2"
    # Force checkout the branch and ensure it tracks the remote branch
    if ! run_cmd git checkout --force -B "$branch" "$remote_name/$branch"; then
        handle_failure "Failed to checkout and update \`$branch\` branch."
    fi
}

# Function to get the commit range between branches
# This function returns the commit range between the main and release branches
# as a string that can be used in Git commands.
get_commit_range() {
    local main_branch="$1"
    local release_branch="$2"
    local remote_name="$3"

    # Return the commit range for the given branches
    echo "$remote_name/$main_branch..$remote_name/$release_branch"
}

# Function to get a list of deleted files between two branches
# This function checks the differences between two branches and extracts the files
# that have been deleted in the release branch compared to the main branch.
get_deleted_files() {
    local branch_main="$1"
    local branch_release="$2"
    local deleted_files

    deleted_files=$(git diff --name-status "$branch_main" "$branch_release" | awk '$1 == "D" {print $2}')
    echo "$deleted_files"
}

# Function to get a list of renamed files between two branches
# This function checks the differences between two branches and identifies the files
# that have been renamed in the release branch compared to the main branch.
get_renamed_files() {
    local branch_main="$1"
    local branch_release="$2"
    local renamed_files

    renamed_files=$(git diff --name-status -M "$branch_main" "$branch_release" | awk '$1 ~ /^R/ {print $2, $3}')
    echo "$renamed_files"
}

# Function to check branch diffs and append the output to the main message
# This function compares the differences between the release and main branches.
# It appends the diff output to the provided message, which can help in investigating conflicts.
get_branch_diffs() {
    local branch_release="$1"
    local branch_main="$2"
    local remote_name="$3"
    local operation_mode="$4"
    local diff_excluded_files="$5"

    # Append the diff check output directly to the main message
    message="$(check_branch_diffs "$remote_name/$branch_release" "$remote_name/$branch_main" "$operation_mode" "$diff_excluded_files")"

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

    if [[ -n "$status_commit_messages" ]]; then
        # Call the format_commits function from the sourced format-commits.sh script, capture and append its output
        formatted_commits=$(format_commits "$status_commit_messages" "$repository")
        message+="\nThe following commits were included in the merge:\n"
        message+="$formatted_commits"
    fi

    echo "$message"
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

# Function to check if a commit message already exists in a branch
# This function checks if a specific commit message exists in the log of the specified branch.
# If the message is found, the function returns success (0); otherwise, it returns failure (1).
check_commit_message_exists() {
    local commit_message="$1"
    local branch="$2"

    if git log "$branch" --pretty=format:"%s" | grep -Fxq "$commit_message"; then
        return 0
    else
        return 1
    fi
}

# Function to check if the release branch has multiple commits not in the main branch
# This function counts the number of commits in the release branch that are not present in the main branch.
# It returns 0 (true) if there are multiple commits, and 1 (false) if there is one or fewer commits.
check_multiple_commits() {
    local release_branch="$1"
    local main_branch="$2"

    # Count the number of commits ahead of the main branch
    commit_count=$(git rev-list --count "$main_branch".."$release_branch")

    if [ "$commit_count" -gt 1 ]; then
        # More than one commit, return true
        return 0
    else
        # One or fewer commits, return false
        return 1
    fi
}

# Function to extract PR number from commit title
# This function uses a regular expression to extract the PR number from a commit title.
# The PR number is expected to be in the format "(#123)".
extract_pr_number() {
    local commit_title="$1"
    local pr_number=""

    # Extract PR number from commit title using a regular expression
    # that matches "(#[number])" and removes the parentheses.
    pr_number="$(echo "$commit_title" | grep -oE '\(#([0-9]+)\)' | sed 's/[()]//g')"

    echo "$pr_number"
}

# Function to gather commit messages and construct merge commit message
# This function gathers commit messages between the release and main branches.
# It constructs a merge commit message and a status message format for use in logs or notifications.
gather_commit_messages() {
    local branch_release="$1"
    local branch_main="$2"
    local merge_commit_message=""
    local status_message_format=""
    local prs=""

    # Iterate over each commit between main and release branch
    while IFS= read -r commit_hash; do
        # Fetch the full commit information
        commit_info="$(git log --pretty=format:'%H %h %s' -n 1 "$commit_hash")"

        # Extract commit hash, short hash, and title
        commit_hash="$(echo "$commit_info" | cut -d' ' -f1)"
        commit_hash_short="$(echo "$commit_info" | cut -d' ' -f2)"
        commit_title="$(echo "$commit_info" | cut -d' ' -f3-)"

        # Append commit title to the merge commit message
        merge_commit_message+="* $commit_title\n"

        # Prepare Slack message format with commit hash and title
        status_message_format+="$commit_hash $commit_hash_short $commit_title\n"

        # Extract PR number from commit title
        pr_number="$(extract_pr_number "$commit_title")"
        if [[ -n "$pr_number" ]]; then
            prs+="$pr_number "
        fi
    done < <(git rev-list "$branch_main..$branch_release")

    # Add PR numbers to the top of the merge commit message
    if [[ -n "$prs" ]]; then
        merge_commit_message="Merge pull request ${prs} from \`$branch_release\`\n$merge_commit_message"
    else
        merge_commit_message="Merge \`$branch_release\`\n$merge_commit_message"
    fi

    # Create an array with the merge message and status message
    local commit_messages=("$merge_commit_message" "$status_message_format")

    # Echo the array elements, each followed by a newline to ensure they are separated properly
    for msg in "${commit_messages[@]}"; do
        echo "$msg"
    done
}

# Ensures deleted and renamed files are handled correctly during merges.
# In some cases, Git merge may not accurately reflect renames with low similarity or when both
# a file's name and contents have changed. Git might handle these scenarios as a deletion of
# the old file and creation of a new file, which could result in both files existing post-merge.
# This function addresses these inconsistencies, ensuring the Git index is updated accordingly.
handle_deleted_and_renamed_files() {
    local deleted_files="$1"
    local renamed_files="$2"

    # Handle deleted files
    if [[ -n "$deleted_files" ]]; then
        handle_debug "Handling deleted files..."
        echo "$deleted_files" | while read -r file; do
            # Check if the file exists before attempting to remove
            if [[ -f "$file" || -d "$file" ]]; then
                git rm "$file"
            else
                handle_debug "Skipping removal, $file has been already removed internally."
            fi
        done
    fi

    # Handle renamed files
    if [[ -n "$renamed_files" ]]; then
        handle_debug "Handling renamed files..."
        echo "$renamed_files" | while read -r old_file new_file; do
            # Check if the old file exists before attempting to rename
            if [[ -f "$old_file" ]]; then
                git mv "$old_file" "$new_file"
            else
                handle_debug "Skipping rename, $old_file has been already renamed internally."
            fi

        done
    fi

    # Only stage and debug if there are deleted or renamed files
    if [[ -n "$deleted_files" || -n "$renamed_files" ]]; then
        # Stage the changes to be committed
        run_cmd git add .

        # Debug git status to check the deleted and renamed files
        debug_cmd git status
    fi
}

# Function to extract author and date information from a specific commit hash.
# This function is useful for preserving the original author and date when creating a new commit.
# The extracted information is returned in a format that can be passed as options to the git commit command.
extract_author_and_date_info() {
    local last_commit_hash="$1"
    local author_info
    local committer_date
    local author_date_options=""

    # Extract author information in the format: "Author Name <author@example.com>"
    author_info=$(git log -1 --pretty=format:'%an <%ae>' "$last_commit_hash")

    # Extract author date (format respects --date= option)
    committer_date=$(git log -1 --pretty=format:'%ad' "$last_commit_hash")

    # Prepare author and date options for the git commit command only if they are not empty
    if [[ -n "$author_info" && -n "$committer_date" ]]; then
        author_date_options="--author=\"$author_info\" --date=\"$committer_date\""
    fi

    # Return the constructed author and date options (empty if not set)
    echo "$author_date_options"
}

# Function to commit changes using a temporary file for the commit message
# Creates a temporary file to store the commit message, ensuring that multiline or complex formatting is preserved exactly as intended.
# This is particularly useful for detailed or formatted commit messages that need to be maintained as is.
# The temporary file is deleted after committing to keep the working environment clean.
# Includes author and date options in the commit command if provided.
commit_with_temp_file() {
    local commit_message="$1"
    local author_date_options="${2:-""}"
    local temp_commit_file

    # Create a temporary file
    temp_commit_file=$(mktemp)

    # Write the commit message to the temporary file
    echo -e "$commit_message" >"$temp_commit_file"

    # Run the git commit command using the temporary file and optional author/date options
    run_cmd git commit --file="$temp_commit_file" "$author_date_options"

    # Clean up the temporary file
    rm -f "$temp_commit_file"
}
