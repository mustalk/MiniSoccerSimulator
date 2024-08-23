#!/bin/bash
# Copyright 2024 MusTalK (https://github.com/mustalk)
#
# Check Branch Differences Script
#
# This script checks for differences between two branches, focusing on identifying unmerged commits
# and file content differences. It considers potential hash changes introduced by rebasing and aims
# to provide accurate and informative output to help identify and resolve potential issues.
#
# Inputs:
#   $1: Release branch name
#   $2: Main branch name
#   $3: Script mode ('merge' or 'sync') to indicate the calling context and adjust behavior accordingly
#
# Outputs:
#   - Prints a message to the console indicating success or warning, including details about unmerged commits and file differences.
#
# Functions:
#   - check_branch_diffs: Main function that orchestrates the script's execution.
#   - check_required_vars: Checks if all required environment variables are set.
#   - sanitize_message: Sanitizes log messages by removing leading colons and replacing newlines with '\n'.
#   - format_diff_tree: Formats and aligns file content differences using awk.
#   - process_diff_tree: Processes and handles file content differences.
#   - process_unmerged_commits: Handles unmerged commits and content differences.
#   - check_unmerged_commits: Identifies unmerged commits between branches, filtering equivalent commits by hash.
#   - check_diff_tree: Compares file contents between branches using git diff-tree.
#   - process_commits_and_diffs: Analyzes unmerged commits and file content differences, distinguishing between hash-only and actual differences.
#
# Note: The script aims to be comprehensive but might not cover all possible edge cases.
#       Manual investigation might still be required in certain situations.
#
# Enable strict mode
set -euo pipefail

# Function to check if all required environment variables are set
# This function iterates over a list of expected environment variables
# and triggers a failure if any of them are not set.
check_required_vars() {
    local vars=("$@")

    # Check if each required variable is set
    for var in "${vars[@]}"; do
        if [ -z "${!var}" ]; then
            echo "Environment variable $var is not set." >&2
            exit 1
        fi
    done
}

# Function to sanitize log messages by removing leading colons and replacing newlines with '\n'
# This function ensures that log messages are clean and can be used in single-line outputs.
sanitize_message() {
    # 1st sed expression: Removes leading colons (common in Git command outputs)
    # 2nd sed expression: Replaces actual newlines with '\n' to keep messages single-line.
    echo "$1" | sed -e 's/^:\+//' -e ':a;N;$!ba;s/\n/\\n/g'
}

# Function to format and align file content differences using awk
# This function formats the output of git diff-tree for easier readability by aligning and converting status codes.
format_diff_tree() {
    local diff_tree="$1"
    local formatted_diff_tree=""

    # Use awk to format and align the output
    formatted_diff_tree=$(echo "$diff_tree" | awk '
        {
            # Convert status to readable format
            if ($1 == "M") $1 = "Modified"
            else if ($1 == "A") $1 = "Added"
            else if ($1 == "D") $1 = "Deleted"

            # Print the formatted string with alignment
            printf "%-8s: %s\n", $1, $2
        }
    ')

    # Sanitize the formatted diff tree to ensure compatibility with Slack's message formatting
    sanitized_diff_tree=$(sanitize_message "$formatted_diff_tree")

    # Add backticks to create a code block in Slack
    echo "\`\`\`$sanitized_diff_tree\`\`\`"
}

# Function to process and handle file content differences
# This function checks for file content differences between the branches and logs warnings if necessary.
process_diff_tree() {
    local diff_tree="$1"
    local main_branch="$2"
    local release_branch="$3"
    local operation_mode="$4"
    local diff_message=""

    if [[ -n "$diff_tree" ]]; then
        local formatted_diff_tree
        formatted_diff_tree=$(format_diff_tree "$diff_tree")

        # If differences are detected, log a warning
        diff_message+="\nWARNING: Differences in file contents between \`$main_branch\` and \`$release_branch\`:\n"
        diff_message+="$formatted_diff_tree\n\nPlease investigate, and resolve manually if needed."
    else
        if [[ "$operation_mode" == "sync" ]]; then
            # If only hash differences are detected, log an informational message
            diff_message+="\nINFO: The only difference is in commit hashes due to rebasing, no file differences detected.\n"
        fi
    fi

    echo "$diff_message"
}

# Function to handle unmerged commits and content differences
# This function processes unmerged commits that have actual content or title differences.
process_unmerged_commits() {
    local unmerged_commits_with_changes="$1"
    local diff_tree="$2"
    local main_branch="$3"
    local release_branch="$4"
    local operation_mode="$5"
    local diff_message=""

    if [[ -n "$unmerged_commits_with_changes" ]]; then
        # Log a warning if unmerged commits are detected
        diff_message+="\nWARNING: Unmerged commits and file content differences between \`$release_branch\` and \`$main_branch\`:\n"
        diff_message+="\n*Unmerged commits from \`$release_branch\` not present in \`$main_branch\`:*\n"
        diff_message+="$unmerged_commits_with_changes\n"

        if [[ -n "$diff_tree" ]]; then
            local formatted_diff_tree
            formatted_diff_tree=$(format_diff_tree "$diff_tree")
            diff_message+="\n*Differences in file contents:*\n"
            diff_message+="$formatted_diff_tree\n"
        fi
    else
        # If no unmerged commits are detected, process file differences
        process_diff_tree "$diff_tree" "$main_branch" "$release_branch" "$operation_mode"
    fi

    echo "$diff_message"
}

# Function to check for differences in commit histories
# This function identifies unmerged commits between the release and main branches, filtering equivalent commits by hash.
check_unmerged_commits() {
    local main_branch="$1"
    local release_branch="$2"
    git log "$main_branch".."$release_branch" --oneline --cherry
}

# Function to check for differences in file contents
# This function compares file contents between the release and main branches using git diff-tree.
check_diff_tree() {
    local main_branch="$1"
    local release_branch="$2"
    git diff-tree -r --name-status "$main_branch" "$release_branch"
}

# Function to process the commit differences and handle them accordingly
# This function analyzes unmerged commits and file content differences, distinguishing between hash-only and actual differences.
process_commits_and_diffs() {
    local unmerged_commits="$1"
    local diff_tree="$2"
    local main_branch="$3"
    local release_branch="$4"
    local operation_mode="$5"

    # Initialize flags and containers for processing commits
    local hash_diff_only=true
    local unmerged_commits_with_changes=""

    # Process each unmerged commit
    if [[ -n "$unmerged_commits" ]]; then
        while IFS= read -r line; do
            # Extract the commit hash from the log line
            local commit_hash
            commit_hash=$(echo "$line" | awk '{print $2}')
            # Retrieve the commit title from the release branch
            local commit_title_release
            commit_title_release=$(git log -1 --format="%s" "$commit_hash")

            # Find a corresponding commit on the main branch with the same title
            local corresponding_commit_hash
            corresponding_commit_hash=$(git log "$main_branch" --format="%H" --grep="$commit_title_release" -n 1)

            if [[ -z "$corresponding_commit_hash" ]]; then
                # If no matching commit is found, consider it an unmerged commit with changes
                unmerged_commits_with_changes+="$line\n"
                hash_diff_only=false
            else
                # Check if there are content differences between the commits
                local commit_diff
                commit_diff=$(git diff-tree --no-commit-id --name-only -r "$commit_hash")
                if ! git diff-tree --quiet "$commit_hash" "$corresponding_commit_hash" -- "$commit_diff"; then
                    # If content differences exist, add to the unmerged list with changes
                    unmerged_commits_with_changes+="$line\n"
                    hash_diff_only=false
                else
                    # If only the hash differs, no further action is needed
                    hash_diff_only=true
                fi
            fi
            # Process each line of 'unmerged_commits' by passing it to the while loop via process substitution.
        done < <(echo "$unmerged_commits")
    fi

    # Process the results based on unmerged commits with changes and differences in file contents
    if [[ "$hash_diff_only" == false ]]; then
        process_unmerged_commits "$unmerged_commits_with_changes" "$diff_tree" "$main_branch" "$release_branch" "$operation_mode"
    else
        # Check for file content differences
        process_diff_tree "$diff_tree" "$main_branch" "$release_branch" "$operation_mode"
    fi
}

# Main function to orchestrate the script's execution
# This function serves as the entry point of the script, handling argument parsing and calling the necessary functions.
check_branch_diffs() {
    local release_branch="$1"
    local main_branch="$2"
    local operation_mode="$3"

    # Initialize variables to store unmerged commits and file differences
    local unmerged_commits=""
    local diff_tree=""

    # Ensure that the required variables are set; handle failure if any are missing
    check_required_vars "release_branch" "main_branch" "operation_mode"

    # Retrieve the list of unmerged commits between the release and main branches
    unmerged_commits=$(check_unmerged_commits "$main_branch" "$release_branch")

    # Retrieve the file content differences between the release and main branches
    diff_tree=$(check_diff_tree "$main_branch" "$release_branch")

    # Process the commits and file differences to determine the appropriate message and status
    process_commits_and_diffs "$unmerged_commits" "$diff_tree" "$main_branch" "$release_branch" "$operation_mode"
}
