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

class ApiClient {

    companion object {
        // For development, point this to your local server, e.g. http://localhost:3000/api
        // For prod, https://kiosk-admin-z8yc.onrender.com/api
        private const val BASE_SERVER_URL = "http://localhost:3000/api"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun activateDevice(code: String, deviceId: String): Result<ActivationResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val requestBody = ActivationRequest(code = code, deviceId = deviceId)
                val activationUrl = "$BASE_SERVER_URL/validate"

                val jsonBody = JSONObject().apply {
                    put("code", requestBody.code)
                    put("deviceId", requestBody.deviceId)
                }.toString()

                val request = Request.Builder()
                    .url(activationUrl)
                    .post(jsonBody.toRequestBody(jsonMediaType))
                    .build()
                Log.d("Api", jsonBody)

                executeActivationRequest(request)
            } catch (e: Exception) {
                Log.e("ApiClient", "Error activating device", e)
                Result.failure(e)
            }
        }
    }

    suspend fun validateActivationCode(code: String, deviceId: String): Result<ActivationResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val validateUrl = "$BASE_SERVER_URL/validate?code=$code&deviceId=$deviceId"

                val request = Request.Builder()
                    .url(validateUrl)
                    .get()
                    .build()

                executeActivationRequest(request)
            } catch (e: Exception) {
                Log.e("ApiClient", "Error validating activation code", e)
                Result.failure(e)
            }
        }
    }

    private fun executeActivationRequest(request: Request): Result<ActivationResponse> {
        Log.d("Api", request.toString())
        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            return Result.failure(IOException("HTTP ${response.code}"))
        }

        val responseBody = response.body?.string() ?: return Result.failure(IOException("Empty response"))
        val jsonResponse = JSONObject(responseBody)

        Log.d("ApiClient", jsonResponse.toString())

        val activationResponse = ActivationResponse(
            valid = jsonResponse.getBoolean("valid"),
            code = jsonResponse.getString("code").takeIf { it.isNotEmpty() },
            reason = jsonResponse.optString("reason").takeIf { it.isNotEmpty() },
            assigned = jsonResponse.optBoolean("assigned", false),
            expiresAt = jsonResponse.optString("expiresAt").takeIf { it.isNotEmpty() }
        )

        return Result.success(activationResponse)
    }
}
