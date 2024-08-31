#!/bin/bash
# Copyright 2024 MusTalK (https://github.com/mustalk)
#
# Update Version Script
#
# This script updates the version information in a specified file (e.g., `app/build.gradle.kts`).
# It extracts the major, minor, and patch components from the provided new version string and updates the
# corresponding version fields in the target file.
#
# It is designed to be used with `semantic-release` to automate version bumping in CI/CD workflows.
# See the `semantic-release` config file `.releaserc.js` for an usage example.
#
# Inputs:
#   <new_version>: The new version number in the format `MAJOR.MINOR.PATCH` (e.g., `1.2.3`).
#   <file_path>: The path to the file that needs version updates (e.g., `app/build.gradle.kts`).
#
# Outputs:
#   NEXT_RELEASE_VERSION: An environment variable containing the new version number that was applied.
#
# Usage:
#     ./update-version.sh <new_version> <file_path>
#
# Enable strict mode
set -euo pipefail

# Print usage information and exit if the number of arguments is incorrect
function validate_input_args() {
    # Check if the number of arguments is not equal to 2
    if [ "$#" -ne 2 ]; then
        # Print the usage message to standard error
        echo "Usage: $0 <new_version> <file_path>" >&2
        # Exit with a non-zero status code
        exit 1
    fi
}

# Function to handle info
handle_info() {
    echo "INFO: $1" >&2
}

# Extract version components from the provided version string
function extract_version_components() {
    local version="$1"
    # Extract the major, minor, and patch version components
    major=$(echo "$version" | cut -d. -f1)
    minor=$(echo "$version" | cut -d. -f2)
    patch=$(echo "$version" | cut -d. -f3)

    # Print the extracted version components to standard error
    handle_info "New version components - Major: $major, Minor: $minor, Patch: $patch"
}

# Function to export the version name to the GitHub Actions environment
export_next_release_version() {
    local version_name="$1"
    echo "NEXT_RELEASE_VERSION=$version_name" >>"$GITHUB_ENV"
}

# Execute the sed command with the appropriate syntax based on the operating system
function exec_sed() {
    local sed_expression="$1"
    local file_path="$2"

    # Check if the operating system is macOS
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS uses a slightly different syntax for the `sed` command (in-place editing without backup)
        sed -i '' "$sed_expression" "$file_path"
    else
        # Linux and other Unix-like systems use the standard `sed` command for in-place editing
        sed -i "$sed_expression" "$file_path"
    fi
}

# Update the version in the specified file
function update_version_in_file() {
    local file_path="$1"

    # Update the major, minor, and patch version in the file using `exec_sed`
    exec_sed "s/versionMajor = .*/versionMajor = $major/g" "$file_path"
    exec_sed "s/versionMinor = .*/versionMinor = $minor/g" "$file_path"
    exec_sed "s/versionPatch = .*/versionPatch = $patch/g" "$file_path"

    # Print a message indicating the successful version update to standard error
    handle_info "Version updated to $major.$minor.$patch in $file_path."
}

# Main function to execute the version update process
function main() {
    # Validate input arguments
    validate_input_args "$@"

    # Capture Script arguments
    local new_version="$1"
    local file_path="$2"

    # Extract version components from the new version
    extract_version_components "$new_version"

    # Export the version name to GitHub Actions environment
    export_next_release_version "$new_version"

    # Update the version in the specified file
    update_version_in_file "$file_path"
}

# Entry point
main "$@"
