#!/bin/bash
# Copyright 2024 MusTalK (https://github.com/mustalk)
#
# Script to grant execute permissions to all scripts in the defined directory below.
#
# Enable strict mode
set -euo pipefail

# Define the base directory for scripts
SCRIPTS_DIR=".github/scripts"

# Check if the scripts directory exists
if [[ ! -d "$SCRIPTS_DIR" ]]; then
    echo "Error: Scripts directory '$SCRIPTS_DIR' not found." >&2
    exit 1
fi

# Grant execute permissions to all .sh scripts in the scripts directory and its subdirectories.
if ! find "$SCRIPTS_DIR" -type f -name "*.sh" -exec chmod +x {} \;; then
    echo "Error: Failed to grant execute permissions to scripts in '$SCRIPTS_DIR'." >&2
    exit 1
fi
