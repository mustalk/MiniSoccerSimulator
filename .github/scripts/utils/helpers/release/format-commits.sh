#!/bin/bash
# Copyright 2024 MusTalK (https://github.com/mustalk)
#
# Format Commit Messages Script
#
# This script formats commit messages for display in Slack(or other platform) notifications.
#
# It takes a list of commit messages as input and generates a formatted message with:
# - Markdown links to the commit on GitHub.
# - Markdown links to the pull request (if mentioned in the commit message).
#
# This ensures that the commit messages are presented in a clear and informative way,
# allowing users to easily access the relevant commits and pull requests on GitHub.
#
# Inputs
# $1 - A list of commit messages, where each message includes a commit hash.
# $2 - The GitHub repository in the format "owner/repo".
#
# Outputs:
# - A formatted string of commit messages with GitHub links suitable for Slack notifications.
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
            echo "Usage: $0 <commit_messages> <github_repository>" >&2
            exit 1
        fi
    done
}

# Function to check if a line is a valid commit message
# This function verifies if the line starts with a commit hash.
is_commit_message() {
    # A simple check to see if the line starts with a commit hash
    # Matches a line starting with a commit hash (7-40 hexadecimal characters: 0-9 and a-f), followed by a space.
    [[ "$1" =~ ^[0-9a-f]{7,40}\  ]]
}

# Main function to format commit messages
# This function processes commit messages, adding Markdown links to GitHub commits and pull requests.
format_commits() {
    local commit_messages="$1"
    local github_repository="$2"
    local formatted_message=""

    # Ensure required arguments are provided; handle failure if any are missing
    check_required_vars "commit_messages" "github_repository"

    if [[ -n "$commit_messages" ]]; then
        # Use a temporary file to handle newlines properly
        temp_file=$(mktemp)
        echo -e "$commit_messages" >"$temp_file"

        # Loop through each line of the temporary file and process only valid commit lines
        while IFS= read -r line; do
            if is_commit_message "$line"; then
                local commit_hash
                local commit_hash_short
                local commit_message
                local pr_number
                local pr_url

                commit_hash=$(echo "$line" | cut -d' ' -f1)
                commit_hash_short=$(echo "$line" | cut -d' ' -f2)
                commit_message=$(echo "$line" | cut -d' ' -f3-)

                # Extract PR number from commit message if present
                pr_number=$(echo "$commit_message" | awk -F'[()]' '{ for(i=1;i<=NF;i++) if ($i~/^#[0-9]+$/) print $i}')

                # If a PR number is found, create a Markdown link for it
                if [[ -n "$pr_number" ]]; then
                    pr_url="https://github.com/$github_repository/pull/${pr_number#\#}"
                    commit_message="${commit_message//($pr_number)/([$pr_number]($pr_url))}"
                fi

                # Append formatted commit message with GitHub commit link
                formatted_message+="* $commit_message ([$commit_hash_short](https://github.com/$github_repository/commit/$commit_hash))\n"
            fi
        done <"$temp_file"

        # Clean up the temporary file
        rm "$temp_file"
    fi

    # Output the formatted message
    echo "$formatted_message"
}
