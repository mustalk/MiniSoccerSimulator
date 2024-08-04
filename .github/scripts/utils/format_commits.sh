#!/bin/bash

COMMIT_MESSAGES=$1
formatted_message=""

# Append commit messages if any
if [[ -n "$COMMIT_MESSAGES" ]]; then
     # Loop through each commit message
    while IFS= read -r line; do
        # Extract commit hash, short hash, and message
        commit_hash=$(echo "$line" | cut -d' ' -f1)
        commit_hash_short=$(echo "$line" | cut -d' ' -f2)
        commit_message=$(echo "$line" | cut -d' ' -f3-)

        # Extract PR number from commit message using awk
        PR_NUMBER=$(echo "$commit_message" | awk -F'[()]' '{ for(i=1;i<=NF;i++) if ($i~/^#[0-9]+$/) print $i}')

        # If a PR number is found, create a Markdown link for it
        if [[ -n "$PR_NUMBER" ]]; then
            PR_URL="https://github.com/$GITHUB_REPOSITORY/pull/${PR_NUMBER#\#}"
            commit_message="${commit_message//($PR_NUMBER)/([$PR_NUMBER]($PR_URL))}"
        fi

        # Add commit message and links to the message string
        formatted_message+="* $commit_message ([$commit_hash_short](https://github.com/$GITHUB_REPOSITORY/commit/$commit_hash))\n\n"
    done <<< "$COMMIT_MESSAGES"
fi

echo "$formatted_message"
