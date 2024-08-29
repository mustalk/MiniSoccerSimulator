#!/bin/bash
# Copyright 2024 MusTalK (https://github.com/mustalk)
#
# GPG Setup Script
#
# This script sets up the GPG environment for signing commits during the CI process.
# It imports the GPG key, extracts necessary information, and configures Git to use the key for signing.
#
# Environment Variables:
#   BOT_GPG_PKEY: The GPG private key used for signing commits.
#   GPG_PASSPHRASE: The passphrase for the GPG key.
#
# Outputs:
#   Exports Git user and GPG signing configurations for use in the CI process.
#
# Usage:
#   ./setup-gpg.sh
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

# Function to handle informational messages
handle_info() {
    echo "INFO: $1" >&2
}

# Function to import the gpg key
import_gpg_key() {
    local bot_gpg_pkey="$1"
    # Import the provided gpg key
    echo "$bot_gpg_pkey" | gpg --batch --quiet --import || handle_failure "GPG key import failed!"
}

# Function to extract the GPG key fingerprint
extract_fingerprint() {
    local fingerprint
    # Extract gpg fingerprint
    fingerprint=$(gpg --batch --quiet --list-secret-keys --with-colons | grep 'fpr' | head -n 1 | cut -d: -f10)
    [[ -n "$fingerprint" ]] || handle_failure "Fingerprint extraction failed!"
    # Store it into a .fpr file for later use
    echo "$fingerprint" > fingerprint.fpr
    echo "$fingerprint"
}

# Function to extract user name and email from the gpg key
extract_user_info() {
    local user_info
    local user_name
    local user_email

    # Extract the user name and email from the imported gpg key
    user_info=$(gpg --batch --quiet --list-secret-keys --with-colons | grep 'uid' | head -n 1 | cut -d: -f10)
    user_name=$(echo "$user_info" | awk -F '<|>' '{print $1}')
    user_email=$(echo "$user_info" | awk -F '<|>' '{print $2}')

    # Check if user name and email were extracted successfully
    [[ -n "$user_name" && -n "$user_email" ]] || handle_failure "User information extraction failed!"

    echo "$user_name" "$user_email"
}

# Function to configure Git for GPG signing
gpg_git_config() {
    local user_name="$1"
    local user_email="$2"
    local fingerprint="$3"

    git config --global user.name "$user_name" || handle_failure "Setting user.name failed!"
    git config --global user.email "$user_email" || handle_failure "Setting user.email failed!"
    git config --global commit.gpgSign true || handle_failure "Setting commit.gpgSign failed!"
    git config --global user.signingkey "$fingerprint" || handle_failure "Setting user.signingkey failed!"
    git config --global gpg.program gpg || handle_failure "Setting gpg.program failed!"
}

export_author_and_committer(){
    local user_name="$1"
    local user_email="$2"

    # Export the Git environment variables for later use
    {
        echo "export GIT_AUTHOR_NAME=\"$user_name\""
        echo "export GIT_AUTHOR_EMAIL=\"$user_email\""
        echo "export GIT_COMMITTER_NAME=\"$user_name\""
        echo "export GIT_COMMITTER_EMAIL=\"$user_email\""
    } >> git_env.sh

    # Source the Git environment variables
    source git_env.sh
}

# Function to setup GPG agent for passphrase usage
gpg_agent_config() {
    local gpg_passphrase="$1"

    # Configure GPG agent using the passphrase via loopback to sign git operations, we redirect the standard output to /dev/null
    # to suppress unnecessary verbose output, while capturing error messages in the `error_message` variable.
    if ! error_message=$( { echo "$gpg_passphrase" | gpg --batch --quiet --yes --pinentry-mode loopback --passphrase-fd 0 --no-tty --command-fd 0 --sign; } 2>&1 > /dev/null); then
        handle_failure "GPG passphrase setup failed: $error_message"
    fi

    # Setup was completed successfully
    handle_info "GPG setup completed successfully."
}

# Main function to execute the GPG setup process
main() {
    # Define the required environment variables
    local required_vars=(BOT_GPG_PKEY GPG_PASSPHRASE)

    # Validate that required environment variables are set
    check_required_vars "${required_vars[@]}"

    # Capture environment variables
    local gpg_pkey="$1"
    local gpg_passphrase="$2"

    # Import the GPG key
    import_gpg_key "$gpg_pkey" "$gpg_passphrase"

    # Extract the fingerprint and user info
    local fingerprint
    fingerprint=$(extract_fingerprint)

    # Extract user name and email
    local user_name
    local user_email
    read -r user_name user_email < <(extract_user_info)

    # Export and source the Git environment variables
    export_author_and_committer "$user_name" "$user_email"

    # Configure Git for GPG signing
    gpg_git_config "$user_name" "$user_email" "$fingerprint"

    # Configure the GPG agent to use the passphrase
    gpg_agent_config "$gpg_passphrase"
}

# Entry point
main "$BOT_GPG_PKEY" "$GPG_PASSPHRASE"
