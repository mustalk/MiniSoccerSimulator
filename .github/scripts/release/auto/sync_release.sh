#!/bin/bash
# Copyright 2024 MusTalK (https://github.com/mustalk)

# Synchronize Release Script

# This script synchronizes the release branch with the main branch by rebasing and force-pushing.
# For more details, see the 'Branch Synchronization' section on the DEV_WORKFLOW.md at the project root.

# List of required environment variables
required_vars=(RELEASE_BRANCH MAIN_BRANCH REMOTE_NAME)

# Check if each required variable is set
for var in "${required_vars[@]}"; do
    if [ -z "${!var}" ]; then
        echo "Error: Environment variable $var is not set."
        exit 1
    fi
done

# Checkout the release branch
git checkout "$RELEASE_BRANCH"

# Stash any uncommitted changes (usually scripts that have been granted execute permissions, no content changes)
STASH_NAME="auto-release-sync-stash-$(date +%s)"
git stash push -u -m "$STASH_NAME"
# Check if the stash was created successfully
if git stash list | grep -q "$STASH_NAME"; then
    echo "Stash created with name $STASH_NAME"
else
    echo "No changes to stash"
fi

# Rebase the release branch onto the main branch
if ! git rebase "$MAIN_BRANCH"; then
    echo "Error: Failed to rebase release branch onto main."
    exit 1
fi

# Force push the changes to the release branch
if git push "$REMOTE_NAME" "$RELEASE_BRANCH" --force-with-lease; then
    echo "Successfully rebased and synchronized the release branch with main."
else
    echo "Error: Failed to push changes to the release branch."
    exit 1
fi

# Un-stash previously stashed changes
# Check if the specific stash exists before popping it
if git stash list | grep -q "$STASH_NAME"; then
  echo "Un-stashing changes from $STASH_NAME"
  git stash pop "stash@{0}"
else
  echo "No specific stash to pop"
fi
