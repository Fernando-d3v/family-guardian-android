package com.familyguardian.domain.usecase

import com.familyguardian.domain.model.NotificationEvent
import com.familyguardian.domain.repository.NotificationRepository
import javax.inject.Inject

/**
 * Sends a captured notification event to the remote API.
 * Returns a [Result] so the caller (service) can log failures without crashing.
 */
class SendNotificationEventUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(event: NotificationEvent): Result<Unit> =
        repository.sendEventToApi(event)
}
