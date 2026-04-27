package com.familyguardian.domain.model

/**
 * Pure domain model representing a captured notification event.
 * No framework dependencies — safe to unit test in isolation.
 */
data class NotificationEvent(
    val id: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    /** Expanded notification body (android.bigText extra). Empty when not available. */
    val bigText: String,
    /** Origin of the event, e.g. "notification". Reserved for future input sources. */
    val source: String,
    val timestamp: Long
)
