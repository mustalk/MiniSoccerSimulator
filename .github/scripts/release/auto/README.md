# Rebase and Merge Script

This directory contains a `rebase-merge.sh` script that automates the process of rebasing and merging the release branch onto the main branch.
The idea behind implementing it is to reduce manual intervention and automate the integration of changes from development branches
that are reviewed and merged on the release branch which then have to be merged into the main codebase.

## Motivation

Our goal was to create a streamlined workflow for integrating changes while maintaining a clean commit history. We aimed to minimize manual merges
and reduce the potential for human error. To achieve this, we opted for a rebase-merge --ff-only approach, which integrates changes from the release
branch into the main branch without creating unnecessary merge commits. This approach helps to keep the commit history **clean and linear**,
especially in scenarios with frequent merges, and also avoids cluttering the pull request list with unnecessary PRs.

## Issue

During development, I encountered inconsistent merge conflict behavior between local and CI (Github Actions) environments where the script executed
flawlessly locally, but consistently failed on the CI due to merge conflicts, even with identical changes.
This discrepancy was documented in [Issue #12](https://github.com/mustalk/MiniSoccerSimulator/issues/12).

## Workaround

To address this issue, I implemented a workaround using the default ort rebase strategy with the `-X theirs` option on the `git rebase`.
This option automatically resolves merge conflicts in favor of the release branch, as it is the only entry point to the main branch on our workflow,
and given the `auto/rebase-merge.sh` gets triggered when pull requests targeting the release branch are merged and closed, it means that all conflicts
should have been resolved and consolidated at this point. So we shouldn't have any issues in theory.

You can read more about Git merge strategies in the official docs: [Git Merge Strategies](https://git-scm.com/docs/merge-strategies)

## **!!WARNING!!**

This approach is not suitable for all branching strategies or development workflows. Proceed with caution, as it is generally recommended for
workflows with a predominantly linear Git history and requires careful code reviews to avoid unintended changes. Development processes involving
more complex branching strategies or frequent parallel development might lead to unintended loss of changes or complex merge conflicts. Consider other
approaches and strategies, dedicated merge tools, or services like [Mergify](https://mergify.com/), which offer more advanced features.
`(Disclaimer: This is not a paid promotion or partnership. I am not related to Mergify nor have being paid to promote it)`

## Implications

While this workaround effectively resolves the immediate issue, it's crucial to acknowledge its limitations. The `-X theirs` strategy can lead to
unintended loss of changes if used with the wrong workflow and/or conflicts are not carefully reviewed beforehand, on the release branch. This shouldn't
be the case in our scenario and specific workflow we opted to use for this project.

See [DEV_WORKFLOW.md](../../../../DEV_WORKFLOW.md) for more details about the chosen branching strategy and workflow.

This approach is best suited for smaller teams or projects with a well-defined and linear workflow.
For larger teams with extensive parallel development, alternative branching strategies and tools might be more suitable.

## Script Functionality

The script performs the following steps:

1. Fetches the latest changes from the remote repository.
2. Ensures a clean working directory.
3. Checks out the release and main branches.
4. Rebases the release branch onto the main branch using the default ort strategy with the `-X theirs` option.
5. Performs a fast-forward merge of the rebased release branch into the main branch.
6. Pushes the updated main branch to the remote repository.
7. Handles potential merge conflicts and provides informative output.

## GPG Signing

Commits made during the automated rebase and merge process are signed using GPG to ensure authenticity and integrity.

## Usage

This script is intended to be used in a CI environment and requires the following environment variables:

* `GITHUB_REPOSITORY`: The name of the GitHub repository.
* `RELEASE_BRANCH`: The name of the release branch.
* `MAIN_BRANCH`: The name of the main branch.
* `REMOTE_NAME`: The name of the remote repository (e.g., `origin`).

## Local Script

If you need to resolve complex conflicts and maybe perform an interactive rebase and merge locally, you can use the `local/rebase-merge.sh` script.
This script provides more control over the rebase process and allows for manual conflict resolution.

For detailed usage instructions and information, see the [README for the local script](../local/README.md).
