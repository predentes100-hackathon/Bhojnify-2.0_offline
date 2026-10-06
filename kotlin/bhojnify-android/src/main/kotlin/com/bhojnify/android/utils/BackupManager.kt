package com.bhojnify.android.utils

import android.content.Context
import android.net.Uri
import com.bhojnify.core.model.MessState
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object BackupManager {
    val jsonSerializer = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun exportData(context: Context, uri: Uri, state: MessState): Boolean {
        return try {
            val outputStream: OutputStream? = context.contentResolver.openOutputStream(uri)
            val jsonString = jsonSerializer.encodeToString(MessState.serializer(), state)
            outputStream?.use { it.write(jsonString.toByteArray()) }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun importData(context: Context, uri: Uri): MessState? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val jsonString = inputStream?.bufferedReader()?.use { it.readText() } ?: return null
            jsonSerializer.decodeFromString<MessState>(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
