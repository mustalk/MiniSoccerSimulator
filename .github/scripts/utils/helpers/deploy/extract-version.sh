#!/bin/bash
# Copyright 2024 MusTalK (https://github.com/mustalk)
#
# Extract Version Script
#
# This script extracts the version information from a Gradle Kotlin DSL (`.kts`) file
# and constructs the `CURRENT_RELEASE_VERSION` environment variable for use in GitHub Actions workflows.
#
# The script expects the Gradle file to define version components similar to the following example:
#   `val versionMajor = 1`
#   `val versionMinor = 2`
#   `val versionPatch = 3`
#
# Environment Variables:
#   GRADLE_FILE_PATH: The path to the Gradle Kotlin DSL file containing the version information.
#
# Outputs:
#   CURRENT_RELEASE_VERSION: An environment variable containing the version number in the format:
#                            `MAJOR.MINOR.PATCH` (e.g., `1.2.3`)
#
# Usage:
#   ./extract-version.sh
#
# Enable strict mode
set -euo pipefail

# Function to check if all required environment variables are set
check_required_vars() {
    local vars=("$@")

    # Check if each required variable is set
    for var in "${vars[@]}"; do
        if [ -z "${!var}" ]; then
            handle_failure "Environment variable $var is not set."
        fi
    done
}

# Function to handle failures
handle_failure() {
    echo "ERROR: $1" >&2
    exit 1
}

# Function to handle info
handle_info() {
    echo "INFO: $1" >&2
}

# Function to extract a version component from the Gradle file
extract_version_component() {
    local component_name="$1"
    local gradle_file="$2"
    grep "val $component_name" "$gradle_file" | grep -o '[0-9]\+'
}

# Function to construct the full version name from extracted components
construct_version_name() {
    local major="$1"
    local minor="$2"
    local patch="$3"
    echo "$major.$minor.$patch"
}

# Function to export the version name to the GitHub Actions environment
export_version_name() {
    local version_name="$1"
    echo "CURRENT_RELEASE_VERSION=$version_name" >>"$GITHUB_ENV"
}

# Main function to execute the version extraction and export process
main() {
    # Define the required environment variables
    local required_vars=(GRADLE_FILE_PATH)

    # Validate that required environment variables are set
    check_required_vars "${required_vars[@]}"

    # Capture environment variables
    local gradle_file="$1"

    # Define local version component vars
    local version_major
    local version_minor
    local version_patch

    # Extract version components
    version_major=$(extract_version_component "versionMajor" "$gradle_file")
    version_minor=$(extract_version_component "versionMinor" "$gradle_file")
    version_patch=$(extract_version_component "versionPatch" "$gradle_file")

    # Construct the version name
    local version_name
    version_name=$(construct_version_name "$version_major" "$version_minor" "$version_patch")

    # Export the version name to GitHub Actions environment
    export_version_name "$version_name"

    # Print the version name for debugging purposes
    handle_info "Extracted version name - $version_name"
}

# Entry point
main "$GRADLE_FILE_PATH"
