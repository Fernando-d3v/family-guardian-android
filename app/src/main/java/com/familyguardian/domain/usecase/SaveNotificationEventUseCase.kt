package com.familyguardian.domain.usecase

import com.familyguardian.domain.model.NotificationEvent
import com.familyguardian.domain.repository.NotificationRepository
import javax.inject.Inject

class SaveNotificationEventUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(event: NotificationEvent) =
        repository.saveNotificationEvent(event)
}
