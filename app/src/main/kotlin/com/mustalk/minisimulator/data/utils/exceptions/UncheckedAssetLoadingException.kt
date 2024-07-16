package com.mustalk.minisimulator.data.utils.exceptions

/**
 * @author by MusTalK on 16/07/2024
 *
 * [UncheckedAssetLoadingException] is a runtime exception that wraps an instance of
 * [AssetLoadingException]. This allows [AssetLoadingException] to be thrown in contexts
 * where only unchecked exceptions are permitted, particularly for testing purposes.
 *
 * This exception is intended to be used in unit tests to simulate scenarios where
 * [AssetLoadingException] needs to be thrown without altering the method signatures
 * of the methods being tested.
 *
 * @property cause The underlying [AssetLoadingException] that caused this exception.
 * @constructor Creates a new [UncheckedAssetLoadingException] wrapping the given [AssetLoadingException].
 *
 * @param cause The [AssetLoadingException] to be wrapped by this runtime exception.
 */

class UncheckedAssetLoadingException(
    cause: AssetLoadingException,
) : RuntimeException(cause)
