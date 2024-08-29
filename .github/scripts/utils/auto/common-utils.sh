#!/bin/bash
# Copyright 2024 MusTalK (https://github.com/mustalk)
#
# Common Utility Script
#
# This script provides a collection of utility functions used by other scripts in the repository.
# It includes functions for:
# - Setting up logging
# - Checking environment variables
# - Running commands and capturing output
# - Restoring script permissions
# - Sanitizing log messages
# - Setting output environment variables
# - Handling success, failure, and informational messages
# - Logging debug messages
#
# This script is intended to be sourced by other scripts and should not be executed directly.
#
# Environment Variables:
#   - SCRIPT_OPERATION_MODE: Should be set to "merge" or "sync" by the calling script.
#     Note: it should be set before sourcing the util scripts to be accessible.
#   - CI_RUN: Set to "true" if running in a CI environment.
#   - DEBUG: Set to "true" to enable debug mode.
#
# Dependencies:
#   - This script requires the `helpers/grant-exec-perms.sh` script for restoring permissions.
#
# Enable strict mode
set -euo pipefail

# Determine if running in a CI environment by checking if GITHUB_ENV is set
CI_RUN=${GITHUB_ENV:+true}

# Enable / Disable debug mode for the logs
# Check if DEBUG is already set by the calling script
if [[ -z "${DEBUG}" ]]; then
    # If not set, set and use the default value bellow.
    DEBUG="false"
fi

# Function to set up logging directory and log file
# This function creates a `logs` directory within the repository if it doesn't already exist,
# and sets up a log file with a name based on the current script name.
setup_logging() {
    # Define the base directory for logs within the repository
    local log_base_dir="./logs"
    mkdir -p "$log_base_dir"

    # Generate a log file name based on the script's name
    local file_name
    file_name=$(basename "$0" .sh)
    LOG_FILE="$log_base_dir/${file_name}.log"
}

# Function to check if all required environment variables are set
# This function iterates over a list of expected environment variables
# and triggers a failure if any of them are not set.
check_required_vars() {
    local vars=("$@")

    # Check if each required variable is set
    for var in "${vars[@]}"; do
        if [ -z "${!var}" ]; then
            handle_failure "Environment variable $var is not set." "false"
        fi
    done
}

# Function to run commands and capture output
# This helps avoid capturing unintended output in variables
# The function runs a command and logs its output (both stdout and stderr) to the log file.
# If debugging is enabled, the command itself is also logged.
run_cmd() {
    {
        if [[ $DEBUG == true ]]; then
            # Log the command being executed
            echo "+$*" >&2
        fi

        "$@" 2>&1 # Run the command and capture stdout and stderr
    } | tee -a "$LOG_FILE" >&2
}

# Function to run a command only when debugging is enabled
# This function executes specific commands only if the DEBUG flag is set to true.
# It relies on the run_cmd function to execute the command and handle logging.
# This is useful for commands that should only be executed during debugging.
debug_cmd() {
    if [[ "$DEBUG" == true ]]; then
        run_cmd "$@"
    fi
}

# Function to restore execute permissions to scripts
# This function ensures that the scripts in the `utils` directory have execute permissions.
restore_script_permissions() {
    chmod +x .github/scripts/utils/helpers/grant-exec-perms.sh
    .github/scripts/utils/helpers/grant-exec-perms.sh
}

# Function to sanitize log messages
# This function removes leading colons and replaces actual newlines with '\n'.
# This ensures that log messages are properly formatted for use in environments
# that do not handle multiline messages well, such as logging or environment variables.
sanitize_message() {
    # 1st sed expression: Removes leading colons (common in Git command outputs)
    # 2nd sed expression: Replaces actual newlines with '\n' to keep messages single-line.
    echo "$1" | sed -e 's/^:\+//' -e ':a;N;$!ba;s/\n/\\n/g'
}

# Function to set output environment variables for subsequent workflow steps.
# This function sets environment variables in the GitHub Actions environment
# to communicate the status and message of the current operation.
set_output() {
    # Status of the operation (e.g., "success", "failure"). and message related to the operation.
    local status="$1"
    local message="$2"
    # Only proceed if running in a CI environment
    if [[ "$CI_RUN" == "true" ]]; then
        # Check the SCRIPT_OPERATION_MODE to determine which environment variables to set
        if [[ "$SCRIPT_OPERATION_MODE" == "merge" ]]; then
            echo "PROMOTE_RELEASE_STATUS=$status" >>"$GITHUB_ENV"
            echo "PROMOTE_RELEASE_MESSAGE=$message" >>"$GITHUB_ENV"
        elif [[ "$SCRIPT_OPERATION_MODE" == "sync" ]]; then
            echo "SYNC_RELEASE_STATUS=$status" >>"$GITHUB_ENV"
            echo "SYNC_RELEASE_MESSAGE=$message" >>"$GITHUB_ENV"
        else
            echo "Unknown SCRIPT_OPERATION_MODE: $SCRIPT_OPERATION_MODE" >&2
            exit 1
        fi
    fi
}

# Function to handle failure
# This function is called when an operation fails. It sanitizes the failure message,
# optionally restores script permissions, logs the error message, and exits the script.
handle_failure() {
    local message
    message="$(sanitize_message "$1")"
    local restore_perms=${2:-"true"}
    local error_exit=${3:-"true"}
    set_output "failure" "$message"
    if [[ "$restore_perms" == "true" ]]; then
        restore_script_permissions
    fi

    # Prefix each non-empty line of the message with "ERROR: "
    echo -e "$message" | sed '/^$/!s/^/ERROR: /' >&2
    if [[ "$error_exit" == "true" ]]; then
        exit 1
    fi
}

# Function to handle success
# This function is called when an operation succeeds. It sanitizes the success message,
# optionally restores script permissions, and logs the success message. If the message
# contains "WARNING:", the status is set to "warning" instead of "success".
handle_success() {
    local message
    message="$(sanitize_message "$1")"
    local restore_perms=${2:-"true"}

    # Set output to "warning" if the message contains "WARNING:"
    if echo "$message" | grep -q "WARNING:"; then
        set_output "warning" "$message"
    else
        set_output "success" "$message"
    fi

    if [[ "$restore_perms" == "true" ]]; then
        restore_script_permissions
    fi
    # Prefix each non-empty line of the message with "SUCCESS: "
    echo -e "$message" | sed '/^$/!s/^/SUCCESS: /' >&2
    exit 0
}

# Function to log warning messages
# This function sanitizes and logs a warning message.
handle_warning() {
    local message
    message=$(sanitize_message "$1")

    # Prefix each non-empty line of the message with "WARNING: "
    echo -e "$message" | sed '/^$/!s/^/WARNING: /' >&2
}

# Function to handle informational messages
# This function logs informational messages prefixed with "INFO:".
# The messages are sanitized and sent to stderr.
handle_info() {
    local message
    message="$(sanitize_message "$1")"
    echo -e "$message" | sed '/^$/!s/^/INFO: /' >&2
}

# Function to log debug messages directly to stderr
# If debugging is enabled (DEBUG=true), this function will output debug messages.
# The messages are printed directly to stderr.
handle_debug() {
    if [[ $DEBUG == true ]]; then
        echo -e "$@" | sed '/^$/!s/^/DEBUG: /' >&2
    fi
}
