#!/bin/bash
# Copyright 2024 MusTalK (https://github.com/mustalk)

# This script sets up the GPG environment for signing commits during the CI process.
# It imports the GPG key, extracts necessary information, and configures Git to use the key for signing.

set -euo pipefail # Enable strict mode

# Check if BOT_GPG_PKEY is set
if [ -z "${BOT_GPG_PKEY}" ]; then
    echo "Error: Environment variable BOT_GPG_PKEY is not set."
    exit 1
fi

# Check if GPG_PASSPHRASE is set
if [ -z "${GPG_PASSPHRASE}" ]; then
    echo "Error: Environment variable GPG_PASSPHRASE is not set."
    exit 1
fi

# Import GPG key
echo "${BOT_GPG_PKEY}" | gpg --batch --quiet --import || { echo "GPG key import failed!"; exit 1; }

# Extract fingerprint, and store it for later usage
FINGERPRINT=$(gpg --batch --quiet --list-secret-keys --with-colons | grep 'fpr' | head -n 1 | cut -d: -f10)
[[ -n "$FINGERPRINT" ]] || { echo "Fingerprint extraction failed!"; exit 1; }
echo "$FINGERPRINT" > fingerprint.fpr

# Extract user name and email
USER_INFO=$(gpg --batch --quiet --list-secret-keys --with-colons | grep 'uid' | head -n 1 | cut -d: -f10)
USER_NAME=$(echo "$USER_INFO" | awk -F '<|>' '{print $1}')
USER_EMAIL=$(echo "$USER_INFO" | awk -F '<|>' '{print $2}')

# Check if user information was extracted
[[ -n "$USER_NAME" && -n "$USER_EMAIL" ]] || { echo "User information extraction failed!"; exit 1; }

# Set Git config variables, needed by the semantic-release plugin, for commit signing
{
    echo "export GIT_AUTHOR_NAME=\"$USER_NAME\""
    echo "export GIT_AUTHOR_EMAIL=\"$USER_EMAIL\""
    echo "export GIT_COMMITTER_NAME=\"$USER_NAME\""
    echo "export GIT_COMMITTER_EMAIL=\"$USER_EMAIL\""
} >> git_env.sh

# Source the git_env.sh file to load environment variables.
source git_env.sh

# Configure Git to use the extracted user information and GPG key for commit signing.
git config --global user.name "$GIT_AUTHOR_NAME" || { echo "Setting user.name failed!"; exit 1; }
git config --global user.email "$GIT_AUTHOR_EMAIL" || { echo "Setting user.email failed!"; exit 1; }
git config --global commit.gpgSign true || { echo "Setting commit.gpgSign failed!"; exit 1; }
git config --global user.signingkey "$FINGERPRINT" || { echo "Setting user.signingkey failed!"; exit 1; }

# Set the GPG program to use for Git
git config --global gpg.program gpg || { echo "Setting gpg.program failed!"; exit 1; }

# Configure GPG agent to use the passphrase via loopback
if ! echo "$GPG_PASSPHRASE" | gpg --batch --quiet --yes --pinentry-mode loopback --passphrase-fd 0 --no-tty --command-fd 0 --sign; then
    echo "GPG passphrase setup failed!";
    exit 1
fi

echo "GPG setup completed successfully."
