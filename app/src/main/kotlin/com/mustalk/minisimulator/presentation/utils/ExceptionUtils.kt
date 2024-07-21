package com.mustalk.minisimulator.presentation.utils

import com.mustalk.minisimulator.presentation.utils.exceptions.UnexpectedException

/**
 * A utility object for handling exceptions.
 *
 * Provides utility functions to simplify exception handling. It includes methods to
 * wrap exceptions in a custom exception type to make error handling more consistent.
 *
 * Note: The `TooGenericExceptionCaught` Detekt rule warns against catching general
 * `Exception` types which might obscure specific exception handling needs.
 * And it's suppressed in this object to allow catching all types of `Exception`.
 * This is done to provide a consistent exception wrapping mechanism.
 *
 * @suppress `TooGenericExceptionCaught`
 *
 * @author by MusTalK on 20/07/2024
 */
@Suppress("TooGenericExceptionCaught")
object ExceptionUtils {
    /**
     * Wraps any exceptions thrown during the execution of the provided action in an [UnexpectedException].
     *
     * This function is useful for ensuring that exceptions are consistently handled and reported
     * as `UnexpectedException` rather than the original exception type. It catches all `Exception` types
     * and rethrows them as `UnexpectedException` with an appropriate message.
     *
     * @param action A lambda function representing the code block to be executed. This code block may throw an exception
     * which will be caught and wrapped in an [UnexpectedException].
     *
     * @return The result of the action if no exceptions are thrown.
     *
     * @throws UnexpectedException If an exception is thrown within the action, it will be wrapped and rethrown as an [UnexpectedException].
     */
    fun <T> wrapInUnexpectedException(action: () -> T): T =
        try {
            action()
        } catch (e: Exception) {
            throw UnexpectedException(e.message ?: "An unexpected error occurred", e)
        }

    /**
     * Wraps any exceptions thrown during the execution of the provided suspending action in an [UnexpectedException].
     *
     * This function is useful for ensuring that exceptions are consistently handled and reported
     * as `UnexpectedException` rather than the original exception type. It catches all `Exception` types
     * and rethrows them as `UnexpectedException` with an appropriate message.
     *
     * @param action A suspending lambda function representing the code block to be executed. This code block may throw an exception
     * which will be caught and wrapped in an [UnexpectedException].
     *
     * @return The result of the action if no exceptions are thrown.
     *
     * @throws UnexpectedException If an exception is thrown within the action, it will be wrapped and rethrown as an [UnexpectedException].
     */
    suspend fun <T> suspendWrapInUnexpectedException(action: suspend () -> T): T =
        try {
            action()
        } catch (e: Exception) {
            throw UnexpectedException(e.message ?: "An unexpected error occurred", e)
        }
}
