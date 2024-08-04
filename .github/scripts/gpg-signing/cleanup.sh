#!/bin/bash
# Copyright 2024 MusTalK (https://github.com/mustalk)

# This script removes the GPG keys and related files used for signing commits during the CI process.
# It ensures that sensitive information is securely deleted after the workflow completes.

# Enable strict mode for more robust error handling
set -euo pipefail

echo "Cleaning up GPG keys..."

# Check if fingerprint.fpr exists
if [ -f fingerprint.fpr ]; then
  # Read the GPG key fingerprint from the fingerprint file
  FINGERPRINT=$(cat fingerprint.fpr)

  #Delete the secret key associated with the fingerprint
  gpg --batch --quiet --yes --delete-secret-keys "$FINGERPRINT" || echo "Failed to delete secret key."

  # Delete the public key associated with the fingerprint
  gpg --batch --quiet --yes --delete-keys "$FINGERPRINT" || echo "Failed to delete public key."

  # Securely delete the fingerprint file
  shred -u fingerprint.fpr
  echo "Temp fingerprint file deleted."
fi

# Securely delete the Git environment file
if [ -f git_env.sh ]; then
  shred -u git_env.sh
  echo "Temp git environment file deleted."
fi

echo "GPG cleanup completed successfully."
