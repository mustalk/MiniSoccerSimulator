#!/bin/bash
# Copyright 2024 MusTalK (https://github.com/mustalk)
#
# GPG Cleanup Script
#
# This script removes the GPG keys and related files used for signing commits during the CI process.
# It ensures that sensitive information is securely deleted after the workflow completes.
#
# Usage:
#   ./cleanup-gpg.sh
#
# Enable strict mode
set -euo pipefail

# Function to handle informational messages
handle_info() {
    echo "INFO: $1" >&2
}

# Function to securely delete a file
secure_delete_file() {
    local file="$1"
    if [ -f "$file" ]; then
        shred -u "$file" || handle_info "Failed to delete $file"
    fi
}

# Function to delete GPG keys by fingerprint
delete_gpg_keys() {
    local fingerprint="$1"

    # Delete the secret key associated with the fingerprint
    gpg --batch --quiet --yes --delete-secret-keys "$fingerprint" || handle_info "Failed to delete secret key."

    # Delete the public key associated with the fingerprint
    gpg --batch --quiet --yes --delete-keys "$fingerprint" || handle_info "Failed to delete public key."
}

# Function to clean up GPG keys and related files
cleanup_gpg() {
    local fingerprint_file="fingerprint.fpr"
    local git_env_file="git_env.sh"

    handle_info "Cleaning up GPG keys..."

    # Check if fingerprint file exists and perform cleanup
    if [ -f "$fingerprint_file" ]; then
        local fingerprint
        fingerprint=$(cat "$fingerprint_file")

        # Delete GPG keys associated with the fingerprint
        delete_gpg_keys "$fingerprint"

        # Securely delete the fingerprint file
        secure_delete_file "$fingerprint_file"
    fi

    # Securely delete the Git environment file
    secure_delete_file "$git_env_file"

    handle_info "GPG cleanup completed successfully."
}

# Main function to execute the cleanup process
main() {
    # Perform GPG and file cleanup
    cleanup_gpg
}

# Entry point
main
