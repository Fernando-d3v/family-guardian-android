package com.familyguardian.data.mapper

import com.familyguardian.data.local.entity.NotificationEntity
import com.familyguardian.data.remote.dto.NotificationEventDto
import com.familyguardian.domain.model.NotificationEvent

// ── Domain → DTO (for API calls) ─────────────────────────────────────────────

/**
 * Maps a domain event to the API wire format.
 * [deviceId] is resolved at the repository layer to keep the domain model clean.
 * Timestamp is converted from milliseconds to seconds for the backend.
 */
fun NotificationEvent.toDto(deviceId: String): NotificationEventDto = NotificationEventDto(
    deviceId = deviceId,
    app = normalizePackageName(packageName),
    packageName = packageName,
    contact = title,
    message = text,
    bigText = bigText,
    timestamp = timestamp / 1000L,
    source = source
)

private fun normalizePackageName(packageName: String): String = when (packageName) {
    "com.whatsapp", "com.whatsapp.w4b" -> "whatsapp"
    "com.instagram.android" -> "instagram"
    "com.facebook.katana" -> "facebook"
    "com.facebook.orca" -> "messenger"
    "org.telegram.messenger" -> "telegram"
    "com.twitter.android", "com.x.android" -> "x"
    "com.snapchat.android" -> "snapchat"
    "com.tiktok.android" -> "tiktok"
    else -> packageName.substringAfterLast(".")
}

// ── Domain → Entity (for Room persistence) ───────────────────────────────────

fun NotificationEvent.toEntity(): NotificationEntity = NotificationEntity(
    id = id,
    packageName = packageName,
    appName = appName,
    title = title,
    text = text,
    bigText = bigText,
    source = source,
    timestamp = timestamp,
    isSent = false
)

// ── Entity → Domain ───────────────────────────────────────────────────────────

fun NotificationEntity.toDomain(): NotificationEvent = NotificationEvent(
    id = id,
    packageName = packageName,
    appName = appName,
    title = title,
    text = text,
    bigText = bigText,
    source = source,
    timestamp = timestamp
)
