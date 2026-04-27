package com.familyguardian.domain.usecase

import com.familyguardian.domain.repository.NotificationRepository
import javax.inject.Inject

/**
 * Triggers a retry pass for all locally stored events that haven't been
 * successfully delivered to the API yet. Called by [SendPendingEventsWorker].
 */
class RetryFailedEventsUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke() = repository.retryFailedEvents()
}
