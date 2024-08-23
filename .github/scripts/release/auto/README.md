# Automated Release Scripts

This directory contains scripts that automate various aspects of the release process, ensuring a streamlined
workflow for integrating changes, reducing manual intervention and maintaining a clean commit history.

## auto/rebase-merge.sh

This script automates the process of rebasing and merging the `release` branch into the `main` branch.
It is designed to be used within a CI/CD pipeline and relies on environment variables set by the workflow,
and is triggered automatically by the CI workflow after a PR targeting the `release` branch is merged and closed.

### Motivation

The goal was to create a streamlined workflow for integrating changes while maintaining a clean commit history
and minimizing manual interventions to reduce the potential for human error. To achieve this, a rebase-merge approach
was chosen, which integrates changes from the release branch into the main branch without creating unnecessary merge commits.
This helps keep the commit history clean and linear, and avoids cluttering the pull request list with unnecessary PRs.

### Issue

During development, inconsistent merge conflict behavior was observed between local and CI (GitHub Actions) environments.
The script worked flawlessly locally but failed on the CI due to merge conflicts, even with identical changes.
This issue is documented in [Issue #12](https://github.com/mustalk/MiniSoccerSimulator/issues/12).

### Workaround

To address this, the script uses the default `ort` rebase strategy with the `-X theirs` option.
This automatically resolves merge conflicts in favor of the `release` branch, which is the only entry point to the main branch in our workflow.
The `auto/rebase-merge.sh` script is triggered when pull requests targeting the `release` branch are merged and closed,
so all conflicts should be resolved and consolidated by then. This approach has proven to be effective for our workflow.

You can read more about Git rebase options in the official docs: [Git rebase options](https://git-scm.com/docs/git-rebase#_options).

### Warning

This approach is suitable for workflows with a predominantly linear Git history and requires careful code reviews to avoid unintended changes.
For more complex branching strategies or extensive parallel development, this approach might lead to unintended loss of changes or
complex merge conflicts if not handled properly. Consider alternative approaches, dedicated merge tools, or services like [Mergify](https://mergify.com/)
for more advanced features.

`(Disclaimer: This is not a paid promotion or partnership. I am not related to Mergify nor have been paid to promote it.)`

### Implications

While this workaround effectively resolves the immediate issue, it's crucial to acknowledge its limitations. The `-X theirs` strategy can lead to
unintended loss of changes if used with the wrong workflow and/or if conflicts are not carefully reviewed beforehand.
This should not be an issue in our scenario and the specific workflow chosen for this project.

See [DEV_WORKFLOW.md](../../../../DEV_WORKFLOW.md) for more details about the chosen branching strategy and workflow.

This approach is best suited for smaller teams or projects with a well-defined and structured workflow.
Larger teams with extensive parallel development might benefit from alternative branching strategies and tools.

### Script Functionality

**What the script does:**

1.  Fetches the latest changes from the remote repository.
2.  Checks if all commits from `release` are already in `main`. If they are, the script stops.
3.  Checks out the `release` and `main` branches, ensuring they are up to date.
4.  Performs a rebase of the `release` branch onto the `main` branch, handling potential merge conflicts using the `-X theirs` strategy.
5.  If there are no conflicts or all conflicts are automatically resolved, performs a fast-forward merge of `release` into `main`.
6.  If there are deleted or renamed files, or multiple commits in the `release` branch, performs a standard merge instead of a rebase.
7.  Pushes the updated `main` branch to the remote repository.
8.  Provides informative messages about the merge status and any potential issues.

### Environment Variables

*   `GITHUB_REPOSITORY`: The GitHub repository name (e.g., "owner/repo").
*   `RELEASE_BRANCH`: The name of the release branch.
*   `MAIN_BRANCH`: The name of the main branch.
*   `REMOTE_NAME`: The name of the remote repository.
*   `SCRIPT_OPERATION_MODE`: The operation mode for the script. Should be set to "merge" or "sync" before sourcing the utility scripts.

### Outputs

*   Sets environment variables (e.g., `PROMOTE_RELEASE_STATUS`, `PROMOTE_RELEASE_MESSAGE`) in CI environments
    to communicate the status and relevant information for subsequent steps in the CI/CD pipeline.

## auto/sync-release.sh

This script synchronizes the `release` branch with the `main` branch by rebasing
and force-pushing (overwriting the remote `release` history with the updated history from the `main` branch).
It ensures that both branches have the same commit history, even if commit hashes differ due to rebasing,
to avoid potential merge conflicts for future merges.
This script is triggered automatically by the CI workflow after a successful rebase-merge to the `main` branch.

### Script Functionality

1.  Checks for unmerged commits and file content differences between the branches.
2.  If no warnings are detected, performs a rebase of the `release` branch onto the `main` branch.
3.  Force-pushes the rebased `release` branch to the remote repository using `--force-with-lease`.
4.  Provides informative messages about the synchronization status and any potential issues.

### Environment Variables

*   `RELEASE_BRANCH`: The name of the release branch.
*   `MAIN_BRANCH`: The name of the main branch.
*   `REMOTE_NAME`: The name of the remote repository (e.g., `origin`).

### Outputs

*   Sets environment variables `SYNC_RELEASE_STATUS` and `SYNC_RELEASE_MESSAGE` in CI environments to communicate the status and relevant information
    for subsequent steps in the CI/CD pipeline.

**Note:** The automated synchronization process ensures that the `release` branch is always up-to-date with the `main` branch,
reducing the risk of merge conflicts and maintaining a clean commit history.

## Utility Scripts

The `auto/rebase-merge.sh` and `auto/sync-release.sh` scripts source the following utility and helper scripts:

*   `utils/auto/core-utils.sh`: Provides Git-related functions and other utilities such as logging, debugging, and permission handling.
*   `utils/auto/common-utils.sh`: Provides additional general-purpose utility functions.
*   `utils/helpers/format-commits.sh`: Provides functions for formatting commit messages.
*   `utils/helpers/check-branch-diffs.sh`: Provides functions for checking and reporting differences between branches.

## GPG Signing

Commits made during the automated process are signed using GPG to ensure authenticity and integrity.

## Local Scripts

Local versions of these scripts are available in the `local` directory.
They provide more control over the process and allow for manual conflict resolution.

For detailed usage instructions and information about the local scripts, see the [README for the local scripts](../local/README.md).

### Conclusion

These automated and local scripts provide a robust and flexible system for managing releases and ensuring code integrity.
By automating the rebase-merge and synchronization processes, these scripts help streamline the development workflow,
minimize the risk of merge conflicts, and maintain a clean and consistent Git history.
For situations requiring more control or manual intervention, the local scripts offer a flexible and convenient alternative
for handling complex merges and resolving conflicts.
