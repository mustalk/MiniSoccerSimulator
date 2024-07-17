package com.mustalk.minisimulator.data.utils.exceptions

/**
 * @author by MusTalK on 16/07/2024
 *
 * Custom exception to represent errors during asset loading
 */

class AssetLoadingException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
