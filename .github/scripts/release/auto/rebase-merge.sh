#!/bin/bash
# Copyright 2024 MusTalK (https://github.com/mustalk)

# Rebase and Merge Script

# This script automates the rebasing and merging of the release branch onto the main branch.
# See the README.md in this directory for detailed documentation and important considerations.

# Enable strict mode
set -euo pipefail

# Function to restore execute permissions to scripts
restore_script_permissions() {
    .github/scripts/utils/grant_exec_perms.sh
}

# Function to set failure output into env. variables, to be used by next workflow steps
set_failure_output() {
    local message=$1
    echo "REBASE_MERGE_STATUS=failure" >> "$GITHUB_ENV"
    echo "REBASE_MERGE_MESSAGE=$message" >> "$GITHUB_ENV"
}

# Function to set success output into env. variables, to be used by next workflow steps
set_success_output() {
    local message=$1
    echo "REBASE_MERGE_STATUS=success" >> "$GITHUB_ENV"
    echo "REBASE_MERGE_MESSAGE=$message" >> "$GITHUB_ENV"
}

# List of required environment variables
required_vars=(GITHUB_REPOSITORY RELEASE_BRANCH MAIN_BRANCH REMOTE_NAME)

# Check if each required variable is set
for var in "${required_vars[@]}"; do
    if [ -z "${!var}" ]; then
        echo "Error: Environment variable $var is not set."
        exit 1
    fi
done

# Make sure we run the script from the root directory of the repository
cd "$(git rev-parse --show-toplevel)"

echo "Starting rebase of $RELEASE_BRANCH onto $MAIN_BRANCH ..."

# Fetch the latest changes from the remote repository
git fetch "$REMOTE_NAME" && echo "Successfully fetched changes from $REMOTE_NAME"

# Perform a dry run of git clean to see which files would be removed
git clean -fd --dry-run

# Ensure a clean working directory, on our CI environment
git reset --hard HEAD

# Remove untracked files and directories from the working directory (if present in any scenario)
git clean -fd

# Checkout the release branch and ensure it tracks the remote branch
git checkout --force -B "$RELEASE_BRANCH" "$REMOTE_NAME/$RELEASE_BRANCH"

# Ensure the branch is up to date
git pull "$REMOTE_NAME" "$RELEASE_BRANCH"

# Checkout the main branch and ensure it tracks the remote branch
git checkout --force -B "$MAIN_BRANCH" "$REMOTE_NAME/$MAIN_BRANCH"

# Ensure the branch is up to date
git pull "$REMOTE_NAME" "$MAIN_BRANCH"

# Switch back to the release branch
git checkout "$RELEASE_BRANCH"

# Get commit messages to be included in the merge
COMMIT_RANGE="$REMOTE_NAME/$MAIN_BRANCH..$REMOTE_NAME/$RELEASE_BRANCH"
COMMIT_MESSAGES=$(git log --pretty="%H %h %s" "$COMMIT_RANGE")

# Ensure the GPG agent is running
gpgconf --launch gpg-agent

# Rebase using the default ort strategy with -X theirs for conflict resolutions, favoring the release branch changes.
# Refer to the README.md in this directory for more details about the reasoning behind using this strategy
if git rebase -X theirs "$MAIN_BRANCH"; then
    echo "Rebase completed successfully."
    # Checkout the main branch
    git checkout "$MAIN_BRANCH"

    # Fast-forward merge to the rebased release branch
    if git merge --ff-only "$RELEASE_BRANCH"; then
        echo "Successfully merged $RELEASE_BRANCH into $MAIN_BRANCH using fast-forward"

        # Push the updated main branch to the remote repository
        git push "$REMOTE_NAME" "$MAIN_BRANCH" && echo "Successfully pushed changes to $REMOTE_NAME/$MAIN_BRANCH"

        # Restore execute permissions to the scripts after the previous checkouts and merge
        restore_script_permissions

        # Store the success message
        message="Rebase and merge of $RELEASE_BRANCH onto $MAIN_BRANCH completed successfully using fast-forward."

        # Append commit messages if any
        if [[ -n "$COMMIT_MESSAGES" ]]; then
            # Format the merged commit messages and append them to the status message
            message+="\n\nThe following commits were included in the merge:\n\n"
            formatted_messages=$(.github/scripts/utils/format_commits.sh "$COMMIT_MESSAGES")
            message+="$formatted_messages"
        fi

        # Set success output
        set_success_output "$message"
    else
        # Fetch the latest changes from the remote repository
        git fetch "$REMOTE_NAME" && echo "Successfully fetched changes from $REMOTE_NAME"

        # Checkout the release branch back to ensure the CI process can continue executing
        # with the files existing on the release branch even if the merge fails.
        git checkout "$RELEASE_BRANCH"

        # Store the failure message
        message="The fast-forward merge of the rebased $RELEASE_BRANCH onto $MAIN_BRANCH failed due to conflicts! Please resolve manually."

        # Set failure output
        set_failure_output "$message"

        # Restore execute permissions to the scripts after the previous checkouts
        restore_script_permissions
    fi
else
    # Store the failure message
    message="Rebase of $RELEASE_BRANCH onto $MAIN_BRANCH failed due to conflicts! Please resolve manually."

    # Abort the rebase on merge conflicts
    git rebase --abort

    # Set failure output
    set_failure_output "$message"

    # Restore execute permissions to the scripts after the previous checkouts
    restore_script_permissions
fi

# Echo the message to the log
echo -e "$message"
