#!/bin/bash
# Copyright 2024 MusTalK (https://github.com/mustalk)

# Local Interactive Rebase and Merge Script

# This script provides an interactive way to rebase and merge a release branch onto the main branch locally.
# See the README.md in this directory for detailed usage instructions and information.

# Enable strict mode
set -euo pipefail

# Set github repository (used to format commit messages)
export GITHUB_REPOSITORY="mustalk/MiniSoccerSimulator"

# Set branch names (modify if needed)
RELEASE_BRANCH="release"
MAIN_BRANCH="main"

# Set remote name (modify if needed)
REMOTE_NAME="origin"

# Set to "true" to enable automatic push after resolving conflicts, "false" to disable
CONFLICT_AUTO_PUSH="false"

# Set to "true" to enable automatic push after successfully rebase-merging , "false" to disable
SUCCESS_AUTO_PUSH="true"

# Function to handle aborting rebase due to user interruption
abort_rebase_user() {
    echo "Aborting rebase due to user interruption..."
    git rebase --abort || true
    echo "Interactive rebase of $RELEASE_BRANCH onto $MAIN_BRANCH was aborted by the user."
    exit 1
}

# Function to handle aborting rebase due to errors
abort_rebase_error() {
    echo "Aborting rebase due to an error..."
    git rebase --abort || true
    echo "Interactive rebase of $RELEASE_BRANCH onto $MAIN_BRANCH failed due to an error."
    exit 1
}

# Function to append commit messages to the message
append_commit_messages() {
    local commit_messages=$1
    local message=$2

    if [[ -n "$commit_messages" ]]; then
        # Check if the format_commits.sh script exists
        if [[ -f ".github/scripts/utils/format_commits.sh" ]]; then
            # Format the merged commit messages and append them to the status message
            message+=$(echo -e "\n\nThe following commits were included in the merge:\n\n")
            merge_commit_messages=$(.github/scripts/utils/format_commits.sh "$commit_messages")
            message+="$merge_commit_messages"
        else
            # Append commit messages with a simple format
            message+=$(echo -e "\n\nThe following commits were included in the merge:\n\n")
            while IFS= read -r commit_message; do
                message+=$(echo -e "- $commit_message \n")
            done <<< "$commit_messages"
        fi
    fi

    echo "$message"
}

# Function to switch back to the starting point branch after the rebase-merge success.
restore_starting_point_branch(){
    git checkout "$original_branch"
    echo "Switched back to the starting point branch: $original_branch"
}

# Make sure we run the script from the root directory of the repository
cd "$(git rev-parse --show-toplevel)"

# Trap SIGINT (Ctrl+C) to abort the rebase if the user stops the script execution
trap 'abort_rebase_user' SIGINT

# Trap ERR to handle errors and abort the rebase
trap 'abort_rebase_error' ERR

original_branch=$(git branch --show-current)
echo "Saved starting point branch name: $original_branch"

echo "Starting interactive rebase of $RELEASE_BRANCH onto $MAIN_BRANCH ..."

# Set the manager helper so it will prompt you for credentials when needed.
git config --global credential.helper manager

# Set Git username and email from global config
git config user.name "$(git config --global user.name)"
git config user.email "$(git config --global user.email)"

# Check if the remote repository exists
if ! git ls-remote "$REMOTE_NAME" &> /dev/null; then
    echo "Error: Repository not found. Please check the remote URL and your permissions."
    exit 1
fi

# Stash any uncommitted changes
STASH_NAME="auto-rebase-merge-stash-$(date +%s)"
git stash push -u -m "$STASH_NAME"
# Check if the stash was created successfully
if git stash list | grep -q "$STASH_NAME"; then
    echo "Stash created with name $STASH_NAME"
else
    echo "No changes to stash"
fi

# Fetch the latest changes from the remote repository
git fetch "$REMOTE_NAME" && echo "Successfully fetched changes from $REMOTE_NAME"

# Checkout the release branch and ensure it tracks the remote branch
git checkout --force -B "$RELEASE_BRANCH" "$REMOTE_NAME/$RELEASE_BRANCH"

# Ensure the branch is up to date
git pull --rebase "$REMOTE_NAME" "$RELEASE_BRANCH"

# Checkout the main branch and ensure it tracks the remote branch
git checkout --force -B "$MAIN_BRANCH" "$REMOTE_NAME/$MAIN_BRANCH"

# Ensure the branch is up to date
git pull --rebase "$REMOTE_NAME" "$MAIN_BRANCH"

# Switch back to the release branch
git checkout "$RELEASE_BRANCH"

# Get commit messages to be included in the merge
COMMIT_RANGE="$REMOTE_NAME/$MAIN_BRANCH..$REMOTE_NAME/$RELEASE_BRANCH"
COMMIT_MESSAGES=$(git log --pretty="%H %h %s" "$COMMIT_RANGE")

# Rebase the release branch onto the main branch interactively
if git rebase -i "$MAIN_BRANCH"; then
    echo "Rebase completed successfully."

    # Checkout the main branch
    git checkout "$MAIN_BRANCH"

    # Attempt a fast-forward merge
    if git merge --ff-only "$RELEASE_BRANCH"; then
        # If there are no new changes on the main branch, the fast-forward merge will succeed.
        echo "Successfully merged $RELEASE_BRANCH into $MAIN_BRANCH"

        # Check if automatic push is enabled
        if [[ "$SUCCESS_AUTO_PUSH" == "true" ]]; then
            # Push the updated main branch to the remote repository
            git push "$REMOTE_NAME" "$MAIN_BRANCH" && echo "Successfully pushed changes to $REMOTE_NAME/$MAIN_BRANCH"
        else
            echo "Automatic push is disabled. Please push the changes manually, you can set SUCCESS_AUTO_PUSH=\"true\" to enable the auto push."
        fi

        # Store the success message
        message="Interactive rebase and merge of $RELEASE_BRANCH onto $MAIN_BRANCH completed successfully."

        # Append commit messages
        message=$(append_commit_messages "$COMMIT_MESSAGES" "$message")

        # Restore the starting point branch
        restore_starting_point_branch
    else
        # If there are new changes on the main branch, the fast-forward merge will fail
        echo "Rebase completed successfully. However, the fast-forward merge failed."
        echo "Please ensure the main branch is up-to-date and try merging again."
        exit 1
    fi
else
    echo "Resolve conflicts manually and continue the rebase."

    while ! git rebase --continue; do
        echo "Rebase conflict resolution in progress. Please resolve conflicts and continue the rebase."
        read -r -p "Press Enter to continue after resolving conflicts..."
    done

    # Checkout the main branch
    git checkout "$MAIN_BRANCH"

    # Attempt a fast-forward merge again after rebase completes
    if git merge --ff-only "$RELEASE_BRANCH"; then
        echo "Successfully merged $RELEASE_BRANCH into $MAIN_BRANCH"

        # Check if automatic push is enabled
        if [[ "$CONFLICT_AUTO_PUSH" == "true" ]]; then
            # Push the updated main branch to the remote repository
            git push "$REMOTE_NAME" "$MAIN_BRANCH" && echo "Successfully pushed changes to $REMOTE_NAME/$MAIN_BRANCH"
        else
            echo "Automatic push is disabled. Please push the changes manually, you can set CONFLICT_AUTO_PUSH=\"true\" to enable the auto push."
        fi


        # Store the success message
        message="Interactive rebase and merge of $RELEASE_BRANCH onto $MAIN_BRANCH completed successfully after conflict resolution."

        # Append commit messages
        message=$(append_commit_messages "$COMMIT_MESSAGES" "$message")

        # Restore the starting point branch
        restore_starting_point_branch
    else
        # Store the failure message
        message="Interactive rebase of $RELEASE_BRANCH onto $MAIN_BRANCH failed."
        echo "Fast-forward merge failed. Please ensure the main branch is up-to-date."
        exit 1
    fi
fi

# Remove the traps if the script completes successfully
trap - SIGINT ERR

# Un-stash previously stashed changes
# Check if the specific stash exists before popping it
if git stash list | grep -q "$STASH_NAME"; then
  echo "Un-stashing changes from $STASH_NAME"
  git stash pop "stash@{0}"
else
  echo "No specific stash to pop"
fi

# Echo the message to the log
echo -e "$message"
