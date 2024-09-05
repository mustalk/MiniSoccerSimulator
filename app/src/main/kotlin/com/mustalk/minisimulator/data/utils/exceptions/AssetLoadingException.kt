package com.mustalk.minisimulator.data.utils.exceptions

/**
 * Custom exception to represent errors during asset loading
 *
 * @author by MusTalK on 16/07/2024
 */

class AssetLoadingException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
