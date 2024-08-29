#!/bin/bash
# Copyright 2024 MusTalK (https://github.com/mustalk)
#
# Grant Execute Permissions Script
#
# This script grants execute permissions to all `.sh` scripts in the specified directory.
#
# Environment Variables:
#   SCRIPTS_DIR: The directory containing the scripts (defaults to `.github/scripts`).
#
# Usage:
#   ./grant-execute-permissions.sh
#
# Enable strict mode
set -euo pipefail

# Function to handle failures
handle_failure() {
    echo "ERROR: $1" >&2
    exit 1
}

# Function to handle informational messages
handle_info() {
    echo "INFO: $1" >&2
}

# Function to check if the scripts directory exists
check_scripts_directory() {
    local scripts_dir="$1"
    if [[ ! -d "$scripts_dir" ]]; then
        handle_failure "Scripts directory '$scripts_dir' not found."
    fi
}

# Function to grant execute permissions to all .sh scripts on the given scripts directory
grant_execute_permissions() {
    local scripts_dir="$1"

    # Search for all `.sh` files in the specified directory and subdirectories, and grant them execute permissions.
    if ! find "$scripts_dir" -type f -name "*.sh" -exec chmod +x {} \;; then
        handle_failure "Failed to grant execute permissions to scripts in '$scripts_dir'."
    fi
    handle_info "Execute permissions granted successfully."
}

# Main function to execute the permission granting process
main() {
    # Get the scripts directory, defaulting to '.github/scripts' if SCRIPTS_DIR environment variable is not set.
    local scripts_dir=${SCRIPTS_DIR:-".github/scripts"}

    # Check if the scripts directory exists
    check_scripts_directory "$scripts_dir"

    # Grant execute permissions to all .sh scripts in the directory
    grant_execute_permissions "$scripts_dir"
}

# Entry point
main
