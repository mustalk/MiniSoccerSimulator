package com.mustalk.minisimulator.data.utils.helpers

import android.content.Context
import java.io.IOException
import java.io.InputStream

/**
 * A utility object for reading JSON files from the assets folder.
 *
 * @author by MusTalK on 16/07/2024
 */
object AssetJsonReader {
    /**
     * Reads a JSON file from the assets folder and returns its content as a String.
     *
     * @param context The application context.
     * @param fileName The name of the JSON file to read.
     * @return The content of the JSON file as a String.
     * @throws IOException If an error occurs while reading the file.
     */
    internal fun readJsonFromAssets(
        context: Context,
        fileName: String,
    ): String {
        val inputStream: InputStream = context.assets.open(fileName)
        return inputStream.bufferedReader().use { it.readText() }
    }
}
