# Local Release Scripts

This directory contains scripts that can be used locally by developers to manage the release process.

## `local/rebase-merge.sh`

This script provides a manual way to rebase and merge the `release` branch into the `main` branch,
which is particularly useful for handling merge conflicts or performing interactive rebases.

**What the Script Does:**

*   Fetches the latest changes from the `origin` remote.
*   Checks if all commits from `release` are already in `main`. If they are, the script stops here, skipping the rebase-merge.
*   Stashes any uncommitted changes.
*   Checks out the `main` branch and updates it.
*   Checks out the `release` branch and updates it.
*   Creates a backup branch of `release`
*   Performs an interactive rebase of the `release` branch onto the `main` branch.
*   Opens an editor where you can choose how to handle each commit (`pick`, `reword`, `edit`, `squash`, `fixup`).
*   If conflicts occur, you’ll be prompted to resolve them manually and continue the rebase.
*   Checks out the `main` branch.
*   Performs checks for unmerged commits and file content differences using `check-branch-diffs.sh`.
*   If no conflicts are found, attempts a fast-forward merge of the rebased `release` branch into the `main` branch.
*   If the merge is successful:
    *   Pushes the changes to the remote repository (if `SUCCESS_AUTO_PUSH` / `CONFLICT_AUTO_PUSH` is set to `"true"`)..
    *   Deletes the backup branch.
    *   Switches back to the original branch from which the script was started.
*   If conflicts were found:
    *   Issues a warning and provides instructions on how to proceed.
    *   Keeps the backup branch for potential recovery.
    *   Switches back to the original branch from which the script was started.

**Usage:**

1.  Open a terminal in the root directory of your Git repository.
2.  Grant execute permission to the script: `chmod +x .github/scripts/release/local/rebase-merge.sh`
3.  Run the script: `./.github/scripts/release/local/rebase-merge.sh`

### Interactive Rebase Cheat Sheet

**Commands:**

* `pick`: Use the commit as-is.
* `reword`: Edit the commit message.
* `edit`: Make changes to the commit content.
* `squash`: Combine the commit with the previous one.
* `fixup`: Combine the commit with the previous one and discard its commit message.

**Example: To squash the last two commits into one**

1. Change the second-to-last commit command to `squash`.
2. Save and close the editor.
3. A new editor will open to edit the combined commit message.

**Note:** This script provides more control over the rebase and merge process compared to the automated script,
allowing developers to handle complex merge scenarios and ensure a clean commit history.

## `local/sync-release.sh`

This script synchronizes the `release` branch with the `main` branch locally.
It’s particularly useful if the automated process fails or if you need to ensure the `release` branch is up-to-date before starting new work.

**What the Script Does:**

*   Fetches the latest changes from the remote repository.
*   Checks if the branches are already synchronized. If they are, the script stops here.
*   Checks out the `release` and `main` branches.
*   Creates a backup branch of `release`
*   Performs checks for unmerged commits and file content differences using `check-branch-diffs.sh`.
*   Stashes any uncommitted changes.
*   If no conflicts are found, rebases the local release branch onto main.
*   If the rebase is successful:
    * Pushes the changes to the remote repository.
    * Deletes the backup branch.
    * Switches back to the original branch from which the script was started.
*   If conflicts were found:
    * Issues a warning and provides instructions on how to proceed.
    * Keeps the backup branch for potential recovery.
    * Switches back to the original branch from which the script was started.

**Usage:**

1.  Open a terminal in the root directory of your Git repository.
2.  Grant execute permission to the script: `chmod +x .github/scripts/release/local/sync-release.sh`
3.  Run the script: `./.github/scripts/release/local/sync-release.sh`

**Note:** This script helps maintain consistency between the local and remote `release` branches,
reducing the risk of merge conflicts and ensuring a smooth workflow.

## Utility Scripts

The `local/rebase-merge.sh` and `local/sync-release.sh` scripts source the following utility and helper scripts:

*   `utils/local/core-utils.sh`: Provides Git-related functions and other utilities such as logging, debugging, and permission handling.
*   `utils/local/common-utils.sh`: Provides additional general-purpose utility functions.
*   `utils/helpers/format-commits.sh`: Provides functions for formatting commit messages.
*   `utils/helpers/check-branch-diffs.sh`: Provides functions for checking and reporting differences between branches.

## GPG Signing

The local scripts respects your global Git configuration for GPG signing. If you have GPG signing enabled globally, your commits will be signed
automatically.

If you want to enable or disable GPG signing for these scripts, you can modify your global Git configuration:

* Run `git config --global commit.gpgsign true` to enable GPG signing
* Run `git config --global commit.gpgsign false` to disable GPG signing

Make sure you have GPG configured correctly before enabling signing.

## Automated Scripts

* For automated rebasing and merging in this project's CI environment (GitHub Actions), the `auto/rebase-merge.sh` script is used.
  This script performs a non-interactive rebase and merge with a specific conflict resolution strategy.

* For automated synchronization of the `release` branch with the `main` branch after a successful merge to `release`, the `auto/sync-release.sh`
  script is used. This script ensures both branches have the same commit history, even if commit hashes differ due to rebasing.

For detailed documentation and important considerations, see the [Automated Scripts README](../auto/README.md).

For a comprehensive overview of the branching strategy and workflow used in this project, including details about the automated release process,
see the [DEV_WORKFLOW.md](../../../../DEV_WORKFLOW.md).
