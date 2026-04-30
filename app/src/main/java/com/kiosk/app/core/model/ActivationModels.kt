package com.kiosk.app.core.model

data class ActivationResponse(
    val valid: Boolean,
    val reason: String? = null,
    val assigned: Boolean = false
)

data class ActivationRequest(
    val code: String,
    val device_id: String
)