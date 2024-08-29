#!/bin/bash
# Copyright 2024 MusTalK (https://github.com/mustalk)
#
# Extract Release Notes Script
#
# This script extracts release notes for a specified version from a Markdown changelog file,
# converts the content to plain text using Perl, and exports it to the GitHub Actions environment.
# Perl is used in this script as it is highly effective for text processing tasks.
#
# Environment Variables:
#   CURRENT_RELEASE_VERSION: The version number for which release notes are to be extracted (e.g., 1.0.0).
#   CHANGELOG_FILE_PATH: The path to the Markdown changelog file.
#
# Outputs:
#   RELEASE_NOTES: An environment variable containing the extracted release notes in plain text format.
#
# Usage:
#   ./extract_release_notes.sh
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

# Function to set the env. variable and exit
export_to_env_and_exit() {
    # Export the release notes to GitHub Actions environment
    {
        echo "RELEASE_NOTES<<EOF"
        echo "$1"
        echo "EOF"
    } >>"$GITHUB_ENV"
    exit 0
}

# Function to convert Markdown to plain text using Perl
# Processes the Markdown content to remove formatting and keep plain text.
convert_to_plain_text() {
    perl -pe '
        # Remove all leading blank lines at the start of the content
        s/\A\s*\n//;

        # Remove markdown headers (lines starting with #)
        s/^[#]* //g;

        # Convert list items from '*', '+' or '-' to '-',
        # and add 4 spaces for indentation
        s/^[*+-] /    - /g;

        # Extract PR references from links, keep them as plain text, and wrap in parentheses
        s/\[([^\]]+)\]\([^\)]+\)/($1)/g;

        # Remove commit references, but keep PR numbers
        s/\(([a-f0-9]{7})\)//g;

        # Remove standalone parentheses
        s/[()]//g;

        # Wrap PR references in parentheses with a leading space
        s/\s+#(\d+)/ (#$1)/g;

        # Trim extra spaces around commas
        s/\s*,\s*/, /g;
    '
}

# Function to extract release notes for the specified version from the changelog file
# Uses `awk` to search for the version section in the changelog and collect the notes.
extract_release_notes() {
    local version="$1"
    local changelog_file="$2"

    # Use `awk` to process the changelog file and extract the release notes.
    awk -v version="$version" '
        # BEGIN block executes before processing any lines
        BEGIN {
            # Flag to indicate if the desired version section has been found.
            found_version = 0;
            # Variable to accumulate the release notes.
            release_notes = "";
        }

        # Check if the current line matches the version header
        # Match version header with or without link
        $0 ~ "## \\[?"version"\\]?" {
            # Set flag to indicate that the desired version section has been found.
            found_version = 1;
            # Skip the current line (which is the version header itself).
            next;
        }

        # If the version section has been found
        found_version {
            # Exit if a new version header is encountered, indicating the end of the section.
            if ($0 ~ /^## [0-9]/) exit;

            # Append the current line to release_notes
            release_notes = release_notes "\n" $0;
        }

        # END block executes after all lines have been processed
        END {
            # Print the collected release notes to standard output.
            print release_notes;
        }
    ' "$changelog_file"
}

# Main function to execute the release notes extraction and conversion
main() {
    # Define the required environment variables
    local required_vars=(CURRENT_RELEASE_VERSION CHANGELOG_FILE_PATH)

    # Validate that required environment variables are set
    check_required_vars "${required_vars[@]}"

    # Capture environment variables
    local version="$1"
    local changelog_file="$2"

    # Extract release notes for the specified version
    local release_notes
    release_notes=$(extract_release_notes "$version" "$changelog_file")

    # Check if we got any notes
    if [ -z "$release_notes" ]; then
        handle_info "No release notes found for version $version"
        export_to_env_and_exit ""
    fi

    # Convert Markdown to plain text and clean up references
    plain_text_notes=$(echo "$release_notes" | convert_to_plain_text)

    # Export plain text notes to to GitHub Actions environment and exit
    export_to_env_and_exit "$plain_text_notes"
}

# Entry point
main "$CURRENT_RELEASE_VERSION" "$CHANGELOG_FILE_PATH"
