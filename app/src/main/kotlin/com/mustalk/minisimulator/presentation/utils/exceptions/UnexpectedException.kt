package com.mustalk.minisimulator.presentation.utils.exceptions

/**
 * Represents an unexpected exception that can occur during runtime.
 *
 * This exception is intended to be used for situations where an unexpected error occurs,
 * and it is not covered by other specific exception types. It extends from [RuntimeException],
 * meaning that it is an unchecked exception and does not need to be declared in method signatures.
 *
 * @property message A detailed message that provides information about the cause of the exception.
 * @property cause The underlying cause of the exception. This can be another throwable that caused
 *                 this exception to be thrown. It can be null if there is no underlying cause.
 *
 * @constructor Creates an instance of [UnexpectedException] with the specified message and optional cause.
 *
 * @param message The detail message for this exception.
 * @param cause The cause of this exception, or null if the cause is nonexistent or unknown.
 *
 * @author by MusTalK on 20/07/2024
 */
class UnexpectedException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
