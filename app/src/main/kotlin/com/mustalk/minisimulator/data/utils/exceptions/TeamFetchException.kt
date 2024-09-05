package com.mustalk.minisimulator.data.utils.exceptions

/**
 * Custom exception to represent errors during team fetching
 *
 * @author by MusTalK on 16/07/2024
 */

class TeamFetchException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
