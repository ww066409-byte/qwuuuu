package com.pashto.quranicgrammar.data.repository

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.pashto.quranicgrammar.data.model.QuranDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

class QuranRepository(private val context: Context) {

    private val gson = Gson()
    private val cachedFileName = "cached_juz_amma.json"
    private val assetFileName = "juz_amma_grammar.json"
    private val TAG = "QuranRepository"

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    suspend fun getDatabase(): QuranDatabase = withContext(Dispatchers.IO) {
        val cachedFile = File(context.filesDir, cachedFileName)
        if (cachedFile.exists() && cachedFile.length() > 0) {
            try {
                Log.d(TAG, "Loading from internal storage: ${cachedFile.absolutePath}")
                cachedFile.bufferedReader().use { reader ->
                    return@withContext gson.fromJson(reader, QuranDatabase::class.java)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed reading cached file, falling back to assets", e)
                cachedFile.delete()
            }
        }

        Log.d(TAG, "Loading fallback from assets: $assetFileName")
        context.assets.open(assetFileName).use { inputStream ->
            InputStreamReader(inputStream, Charsets.UTF_8).use { reader ->
                return@withContext gson.fromJson(reader, QuranDatabase::class.java)
            }
        }
    }

    suspend fun updateDatabaseFromRemote(remoteUrl: String): Result<QuranDatabase> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching remote database from: $remoteUrl")
            val request = Request.Builder()
                .url(remoteUrl)
                .header("Accept", "application/json")
                .header("User-Agent", "QuranicGrammarPashto-Android/1.0")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("سرور ته په رسېدو کې ستونزه: HTTP ${response.code}")
                )
            }

            val jsonString = response.body?.string()
                ?: return@withContext Result.failure(Exception("له سرور څخه تش ځواب تر لاسه شو"))

            val updatedDb = gson.fromJson(jsonString, QuranDatabase::class.java)
            if (updatedDb == null || updatedDb.surahs.isNullOrEmpty()) {
                return@withContext Result.failure(Exception("ډیټا معتبره نه وه یا د سورتونو لست خالي دی"))
            }

            val tempFile = File(context.filesDir, "temp_juz_amma.json")
            FileOutputStream(tempFile).use { fos ->
                fos.write(jsonString.toByteArray(Charsets.UTF_8))
                fos.flush()
            }

            val targetFile = File(context.filesDir, cachedFileName)
            if (tempFile.renameTo(targetFile)) {
                Log.i(TAG, "Database updated successfully to version: ${updatedDb.version}")
                Result.success(updatedDb)
            } else {
                tempFile.copyTo(targetFile, overwrite = true)
                tempFile.delete()
                Result.success(updatedDb)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Remote update failed", e)
            Result.failure(e)
        }
    }

    fun hasCustomCachedDatabase(): Boolean {
        val file = File(context.filesDir, cachedFileName)
        return file.exists() && file.length() > 0
    }

    fun clearCache(): Boolean {
        val file = File(context.filesDir, cachedFileName)
        return if (file.exists()) file.delete() else true
    }
}