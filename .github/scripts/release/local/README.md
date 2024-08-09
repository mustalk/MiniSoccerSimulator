# Local Interactive Rebase and Merge

This script provides a way to perform an interactive rebase and merge of a release branch onto the main branch locally.
It allows for manual conflict resolution and commit manipulation.

## Usage

1. Keep in mind that any uncommitted changes will be automatically stashed at the beginning of the script and restored after the rebase and merge process is complete.
2. Open a terminal in the root directory of your Git repository.
3. Grant execute permission to the script: `chmod +x .github/scripts/release/local/rebase-merge.sh`
4. Run the script: `./.github/scripts/release/local/rebase-merge.sh`

The script will:

* Stash any uncommitted changes.
* Fetch the latest changes from the `origin` remote.
* Checkout the main branch and update it.
* Checkout the release branch and update it.
* Perform an interactive rebase of the release branch onto the main branch.
* Open an editor where you can choose how to handle each commit (pick, reword, edit, squash, fixup).
* If conflicts occur, you'll be prompted to resolve them manually and continue the rebase.
* Attempt a fast-forward merge of the rebased release branch into the main branch.
* If the merge is successful:
    * Push the changes to the remote repository (if `SUCCESS_AUTO_PUSH` / `CONFLICT_AUTO_PUSH` is set to `"true"`).
    * Switch back to the original branch from which the script was started.
    * Un-stash any previously stashed changes.

## Interactive Rebase Cheat Sheet

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

## GPG Signing

This script respects your global Git configuration for GPG signing. If you have GPG signing enabled globally, your commits will be signed automatically.

If you want to enable or disable GPG signing for this script, you can modify your global Git configuration:

* Run `git config --global commit.gpgsign true` to enable GPG signing

* Run `git config --global commit.gpgsign false` to disable GPG signing

Make sure you have GPG configured correctly before enabling signing.

## Automated Script

For automated rebasing and merging in this project's CI environment (GitHub Actions), the `auto/rebase-merge.sh` script is used.
This script performs a non-interactive rebase and merge with a specific conflict resolution strategy.

For detailed documentation and important considerations, see the [Automated Script README](../auto/README.md).
