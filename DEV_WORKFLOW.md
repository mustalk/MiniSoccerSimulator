# Coding Conventions & Development Workflow

This document outlines the coding conventions on this Android project and describes the development workflow,
including branching strategy, commit message conventions, and codestyle guidelines.

## Unified Release Branching Strategy

We use a Git branching model with the following key branches:

- **`main`:** The main branch represents the stable, production-ready codebase.
- **`release`:** A long-lived branch for integrating features, preparing releases, and triggering the CD pipeline.

## Branch Naming Conventions

We follow these conventions for naming branches:

- **`feature/<feature-name>`:** For new features and enhancements.
- **`bugfix/<bug-name>`:** For bug fixes and patches.
- **`chore/<task-name>`:** For maintenance tasks, code style changes, etc.
- **`docs/<doc-name>`:** For documentation updates.
- **`refactor/<refactor-name>`:** For code refactoring.
- **`style/<style-name>`:** For code style changes and formatting.
- **`test/<test-name>`:** For adding or improving tests.
- **`ci/<ci-name>`:** For changes to the CI/CD pipeline.
- **`build/<build-name>`:** For changes to the build system or tooling.
- **`perf/<perf-name>`:** For performance improvements and optimizations.

## Conventional Commits

We use [Conventional Commits](https://www.conventionalcommits.org/) for writing consistent and informative commit messages. This convention helps to:

- **Automate Changelog generation:** The `android-release.yml` workflow uses [semantic-release](https://github.com/semantic-release/semantic-release) to automatically generate release notes and update the `CHANGELOG.md` based on commit messages that follow the Conventional Commits specification.
- **Enforce semantic versioning:** Semantic-release uses the commit message format to determine the appropriate version bump (major, minor, or patch) according to [Semantic Versioning](https://semver.org/) principles.
- **Improve code readability and maintainability:** Consistent commit messages make it easier to understand the history of changes and the reasoning behind them.

**Commit Message Format:**

`type(optional scope): description`

`optional body`

`optional footer(s)`

**Example:**

`feat: Add dark mode support`

`This commit introduces a new dark mode theme for the app.`

`BREAKING CHANGE: The default theme has been changed to dark mode.`

**Common Types:**

- **`feat`:**  A new feature.
- **`feat!`:** A new feature with a breaking change (indicated by the `!`).
- **`fix`:** A bug fix.
- **`chore`:**  Maintenance tasks or code style changes.
- **`chore(release)`:**  Release preparation and automation.
- **`docs`:** Documentation updates.
- **`refactor`:** Code refactoring.
- **`style`:** Code style changes (e.g., formatting).
- **`test`:** Adding or improving tests.
- **`ci`:** Changes to the CI/CD pipeline.
- **`build`:** Changes to the build system or tooling.
- **`perf`:** Performance improvements.

## Workflow

1. **Development:**
    - Create a new branch from `release` using the appropriate naming convention.
    - Develop the feature, bug fix, or other change on your branch.
    - Commit changes with descriptive messages following the Conventional Commits format.
    - Push your feature branch to the remote repository.

2. **Integration:**
    - Create a pull request (PR) to merge your branch into `release`.
    - Ensure all CI checks pass and the code is reviewed.
    - Merge the PR into `release`.

3. **Release Preparation:**
    - The `android-release.yml` workflow runs automatically on every push or (PR) merge to `release`.
    - This workflow uses `semantic-release` to:
        - Analyze commit messages and determine the next version number based on Conventional Commits and Semantic Versioning.
        - Generate release notes and updates the changelog file.
        - Update the app version in `app/build.gradle.kts`.
        - Create a Git tag for the release.
        - Create a new GitHub release.
        - Notify about the release on Slack.

4. **Deployment:**
    - Once the release is ready, create a PR to merge `release` into `main`.
    - After the PR is merged, the `android-deploy.yml` workflow runs automatically.
    - This workflow:
        - Builds the app.
        - Deploys the app to Firebase App Distribution (and easily to Google Play when we're ready to publish).
        - Uploads the APK to the GitHub release assets.

## GitHub Actions Workflows

We use GitHub Actions for continuous integration and deployment. The following workflows are defined:

- **`android-test.yml`:** Runs tests and code analysis on every push to development branches and on PRs to `main` and `release`.
- **`android-release.yml`:** Prepares the release by generating release notes, updating the app version, creating a Git tag, a GitHub release, and
  notifying on Slack. Runs on every push to `release`.
- **`android-deploy.yml`:** Deploys the app to our chosen deployment platform and uploads the APK to GitHub releases. Runs on merged PRs to `main`.

## Code Style and Conventions

This project uses Detekt, Ktlint, and Spotless to enforce consistent code style and coding conventions.

These tools are integrated into the build process and a pre-commit script that automatically checks your code before each commit. So ensure
that your code adheres to the defined style guidelines to avoid any issues during the commit process.

You can manually run these tools using the following commands:

- Detekt: `./gradlew detekt`
- Ktlint: `./gradlew ktlintCheck`
- Spotless: `./gradlew spotlessApply`

Optionally, for a more streamlined workflow, you can configure Android Studio to format your code with Ktlint on file save:

Install the [Detekt](https://plugins.jetbrains.com/plugin/10761-detekt) plugin and optionally the [Ktlint](https://plugins.jetbrains.com/plugin/15057-ktlint)
and/or [Spotless](https://plugins.jetbrains.com/plugin/18321-spotless-gradle) plugins for Android Studio to get
real-time feedback and inspections directly in the IDE.

You can configure and use them as its suits you best

1. Go to **`File` > `Settings` > `Tools` > `Actions on Save`**.
2. Enable the `Format with Ktlint` option.

If you prefer using shortcuts, you can configure keymaps for the installed plugins:

1. Go to **`File` > `Settings` > `Keymap`**.
2. Search for **`Ktlint`** or **`Spotless`** and assign your desired shortcuts to the available actions.

## Important Notes

- Always create a new branch for each new feature or bug fix from the release branch.
- Follow the branch naming conventions.
- Write clear and descriptive commit messages using Conventional Commits.
- Ensure all CI checks pass before merging PRs.