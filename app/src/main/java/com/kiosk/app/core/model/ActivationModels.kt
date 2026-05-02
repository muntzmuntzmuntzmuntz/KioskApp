package com.kiosk.app.core.model

data class ActivationResponse(
    val valid: Boolean,
    val reason: String? = null,
    val code: String? = null,
    val assigned: Boolean = false,
    val expiresAt: String? = null
)

data class ActivationRequest(
    val code: String,
    val deviceId: String
)
