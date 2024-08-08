#!/bin/bash
# Copyright 2024 MusTalK (https://github.com/mustalk)

# Script to grant execute permissions to all our scripts in the defined directory below.

# Define the base directory for scripts
SCRIPTS_DIR=".github/scripts"

# Check if this script itself has execute permission
if [[ ! -x "$0" ]]; then
    if ! chmod +x "$0"; then
        echo "Error: Failed to grant execute permission to this script." >&2
        exit 1
    fi
fi

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

echo "Execute permissions granted successfully on $SCRIPTS_DIR/"

