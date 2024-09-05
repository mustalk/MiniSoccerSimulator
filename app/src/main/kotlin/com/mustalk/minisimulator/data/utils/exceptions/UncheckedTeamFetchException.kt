package com.mustalk.minisimulator.data.utils.exceptions

/**
 * [UncheckedTeamFetchException] is a runtime exception that wraps an instance of
 * [TeamFetchException].
 *
 * This allows `TeamFetchException` to be thrown in contexts
 * where only unchecked exceptions are permitted, particularly for testing purposes.
 *
 * This exception is intended to be used in unit tests to simulate scenarios where
 * `TeamFetchException` needs to be thrown without altering the method signatures
 * of the methods being tested.
 *
 * @property cause The underlying `TeamFetchException` that caused this exception.
 * @constructor Creates a new `UncheckedTeamFetchException` wrapping the given `TeamFetchException`.
 *
 * @param cause The `TeamFetchException` to be wrapped by this runtime exception.
 *
 * @author by MusTalK on 16/07/2024
 */

class UncheckedTeamFetchException(
    cause: TeamFetchException,
) : RuntimeException(cause)
