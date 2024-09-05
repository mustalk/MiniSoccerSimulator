package com.mustalk.minisimulator.data.utils

/**
 * A sealed class representing the result of an API call.
 *
 * This sealed class provides two possible outcomes for an API call:
 * - [Success]: Indicates a successful API call with the deserialized data of type [T].
 * - [Error]: Indicates an error occurred during the API call, wrapping the exception encountered.
 *
 * @param T The type of data expected in the API response for successful calls.
 *
 * @author by MusTalK on 16/07/2024
 */
sealed class ApiResult<out T> {
    /**
     * Represents a successful API call with the deserialized data.
     *
     * @param data The deserialized data of type [T].
     */
    data class Success<out T>(
        val data: T,
    ) : ApiResult<T>()

    /**
     * Represents an error encountered during the API call.
     *
     * @param exception The exception that occurred.
     */
    data class Error(
        val exception: Exception,
    ) : ApiResult<Nothing>()
}
