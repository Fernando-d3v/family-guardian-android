package com.familyguardian.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Wire format sent to POST /api/events.
 *
 * JSON example:
 * {
 *   "device_id": "abc123",
 *   "app": "whatsapp",
 *   "package_name": "com.whatsapp",
 *   "contact": "João Silva",
 *   "message": "Oi, tudo bem?",
 *   "big_text": "",
 *   "timestamp": 1714150000,
 *   "source": "notification"
 * }
 */
@Serializable
data class NotificationEventDto(
    @SerialName("device_id") val deviceId: String,
    @SerialName("app") val app: String,
    @SerialName("package_name") val packageName: String,
    @SerialName("contact") val contact: String,
    @SerialName("message") val message: String,
    @SerialName("big_text") val bigText: String,
    @SerialName("timestamp") val timestamp: Long,
    @SerialName("source") val source: String
)
