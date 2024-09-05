package com.mustalk.minisimulator.data.deserializers

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.mustalk.minisimulator.data.utils.ApiResult
import java.lang.reflect.Type

/**
 * A custom deserializer for the [ApiResult] class.
 *
 * This deserializer handles the deserialization of JSON responses into [ApiResult] objects,
 * which represent the result of an API call. It either returns an [ApiResult.Success] with
 * the deserialized data or an [ApiResult.Error] with the exception encountered during deserialization.
 *
 * @param T The type of data contained in the [ApiResult].
 * @property dataDeserializer The deserializer for the data type [T].
 *
 * @author by MusTalK on 16/07/2024
 */

class ApiResultDeserializer<T>(
    private val dataDeserializer: JsonDeserializer<T>,
) : JsonDeserializer<ApiResult<T>> {
    /**
     * Deserializes a JSON element into an [ApiResult] object.
     *
     * @param json The JSON element to deserialize.
     * @param typeOfT The type of the [ApiResult] object.
     * @param context The deserialization context.
     * @return An [ApiResult] object representing the deserialized API response.
     * @throws JsonParseException If an error occurs during deserialization.
     */
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?,
    ): ApiResult<T> =
        try {
            val data = dataDeserializer.deserialize(json, typeOfT, context)
            ApiResult.Success(data)
        } catch (e: JsonParseException) {
            ApiResult.Error(e)
        }
}
