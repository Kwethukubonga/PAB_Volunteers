package com.kantu.pab_volunteers.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.kantu.pab_volunteers.BuildConfig
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.utils.AppError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Sends a picture to Cloudinary and gives back the link to it. Firestore only stores
 * the link, so the pictures never sit in the database.
 */
object ImageUploader {

    private const val MAX_SIDE = 1280
    private const val JPEG_QUALITY = 80
    private const val TIMEOUT_MILLIS = 30_000

    private val cloudName: String get() = BuildConfig.CLOUDINARY_CLOUD_NAME
    private val uploadPreset: String get() = BuildConfig.CLOUDINARY_UPLOAD_PRESET

    val isConfigured: Boolean get() = cloudName.isNotBlank() && uploadPreset.isNotBlank()

    suspend fun upload(context: Context, uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext Result.failure(AppError(R.string.error_image_not_set_up))
        }
        try {
            val bytes = readScaledJpeg(context, uri)
                ?: return@withContext Result.failure(AppError(R.string.error_image_unreadable))
            Result.success(post(Base64.encodeToString(bytes, Base64.NO_WRAP)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Photos are shrunk before sending so the upload is quick on a phone connection. */
    private fun readScaledJpeg(context: Context, uri: Uri): ByteArray? {
        // This pass only fills in the size. It returns no bitmap, so the stream is
        // what gets checked, not the result.
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        val sizeStream = context.contentResolver.openInputStream(uri) ?: return null
        sizeStream.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSizeFor(bounds.outWidth, bounds.outHeight)
        }
        val bitmap = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: return null

        return ByteArrayOutputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            bitmap.recycle()
            out.toByteArray()
        }
    }

    private fun sampleSizeFor(width: Int, height: Int): Int {
        var sample = 1
        var longest = maxOf(width, height)
        while (longest / 2 >= MAX_SIDE) {
            longest /= 2
            sample *= 2
        }
        return sample
    }

    /** Unsigned upload, so no secret is needed in the app. */
    private fun post(base64Image: String): String {
        val endpoint = "https://api.cloudinary.com/v1_1/$cloudName/image/upload"
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = TIMEOUT_MILLIS
            readTimeout = TIMEOUT_MILLIS
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        }
        try {
            val body = "file=" + URLEncoder.encode("data:image/jpeg;base64,$base64Image", "UTF-8") +
                "&upload_preset=" + URLEncoder.encode(uploadPreset, "UTF-8")
            connection.outputStream.use { it.write(body.toByteArray()) }

            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val response = stream?.bufferedReader()?.use { it.readText() }.orEmpty()

            if (code !in 200..299) throw failureFor(response, code)
            return JSONObject(response).getString("secure_url")
        } finally {
            connection.disconnect()
        }
    }

    /**
     * The service wording is for developers. The cases worth acting on get their own
     * message, and anything else is passed through so the reason is not lost.
     */
    private fun failureFor(response: String, code: Int): Exception {
        val message = runCatching {
            JSONObject(response).getJSONObject("error").getString("message")
        }.getOrNull()

        return when {
            message == null -> AppError(R.string.error_image_upload, listOf(code))
            message.contains("preset", ignoreCase = true) ->
                AppError(R.string.error_image_preset)
            else -> IllegalStateException(message)
        }
    }
}
