package com.mustalk.minisimulator.data.utils.helpers

import android.content.Context
import com.google.gson.GsonBuilder
import com.mustalk.minisimulator.data.deserializers.ApiResultDeserializer
import com.mustalk.minisimulator.data.deserializers.TeamDeserializer
import com.mustalk.minisimulator.data.utils.ApiResult
import com.mustalk.minisimulator.domain.entities.teams.Team
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection.HTTP_OK

/**
 * A utility object for network-related operations.
 *
 * @author by MusTalK on 16/07/2024
 */

object NetworkHelper {
    /**
     * Creates an OkHttp Interceptor that intercepts requests to the "/teams" endpoint
     * and returns a mock response from the assets json file.
     *
     * @param context The application context.
     * @return An OkHttp Interceptor for mocking API responses.
     */
    fun getInterceptor(context: Context): Interceptor =
        Interceptor { chain ->
            val request = chain.request()
            // Intercept requests to the "/teams" endpoint
            if (request.url.encodedPath == "/api/teams") {
                // Mock response, reading from assets json file
                val mockResponse = AssetJsonReader.readJsonFromAssets(context, "teams.json")

                // Return a mock response
                Response
                    .Builder()
                    .code(HTTP_OK)
                    .message("OK")
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .body(mockResponse.toResponseBody("application/json".toMediaTypeOrNull()))
                    .build()
            } else {
                // Continue with the original request, if it's not for the "/teams" endpoint
                chain.proceed(request)
            }
        }

    /**
     * Provides a configured Retrofit instance.
     *
     * @param client The OkHttp client to use.
     * @param teamDeserializer The deserializer for [Team] objects.
     * @param baseUrl The base URL for the API.
     * @return A Retrofit instance ready for API calls.
     */
    fun getRetrofit(
        client: OkHttpClient,
        teamDeserializer: TeamDeserializer,
        baseUrl: String,
    ): Retrofit {
        // Create a Gson instance with the ApiResultDeserializer and the Team deserializer
        val gson =
            GsonBuilder()
                .registerTypeAdapter(ApiResult::class.java, ApiResultDeserializer(teamDeserializer))
                .create()

        // Create a Retrofit instance with the client, Gson, and the base URL
        return Retrofit
            .Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}
