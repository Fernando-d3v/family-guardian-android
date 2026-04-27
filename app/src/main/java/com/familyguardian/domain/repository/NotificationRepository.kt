package com.familyguardian.domain.repository

import com.familyguardian.domain.model.NotificationEvent
import kotlinx.coroutines.flow.Flow

/**
 * Contract for all notification event operations.
 * Implementations live in the data layer; this interface is owned by the domain.
 */
interface NotificationRepository {

    /** Emits the most recent notification events (up to MAX_STORED_EVENTS), newest first. */
    fun getNotificationEvents(): Flow<List<NotificationEvent>>

    /** Persists a new notification event locally (isSent=false) and emits to the event bus. */
    suspend fun saveNotificationEvent(event: NotificationEvent)

    /**
     * Sends a single event to the remote API and marks it as sent in the local DB.
     * Returns a [Result] so callers can decide how to handle failures without try/catch.
     */
    suspend fun sendEventToApi(event: NotificationEvent): Result<Unit>

    /**
     * Fetches all locally stored unsent events and attempts to deliver them to the API.
     * Used by the background WorkManager retry job.
     */
    suspend fun retryFailedEvents()

    /** Hot stream of newly captured events — useful for real-time UI updates. */
    fun observeNewEvents(): Flow<NotificationEvent>
}
