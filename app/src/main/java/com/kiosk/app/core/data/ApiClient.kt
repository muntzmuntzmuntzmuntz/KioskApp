package com.kiosk.app.core.data

import android.util.Log
import com.kiosk.app.core.model.ActivationRequest
import com.kiosk.app.core.model.ActivationResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class ApiClient(private val baseUrl: String) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun validateActivationCode(code: String, deviceId: String): Result<ActivationResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val requestBody = ActivationRequest(code, deviceId)
                val jsonBody = JSONObject().apply {
                    put("code", requestBody.code)
                    put("device_id", requestBody.device_id)
                }.toString()

                val request = Request.Builder()
                    .url("$baseUrl/api/validate")
                    .post(jsonBody.toRequestBody(jsonMediaType))
                    .build()

                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    return@withContext Result.failure(IOException("HTTP ${response.code}"))
                }

                val responseBody = response.body?.string() ?: return@withContext Result.failure(IOException("Empty response"))
                val jsonResponse = JSONObject(responseBody)

                val activationResponse = ActivationResponse(
                    valid = jsonResponse.getBoolean("valid"),
                    reason = jsonResponse.optString("reason", null).takeIf { it.isNotEmpty() },
                    assigned = jsonResponse.optBoolean("assigned", false)
                )

                Result.success(activationResponse)

            } catch (e: Exception) {
                Log.e("ApiClient", "Error validating activation code", e)
                Result.failure(e)
            }
        }
    }
}