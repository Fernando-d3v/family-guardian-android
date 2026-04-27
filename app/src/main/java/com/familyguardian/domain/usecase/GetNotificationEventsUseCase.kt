package com.familyguardian.domain.usecase

import com.familyguardian.domain.model.NotificationEvent
import com.familyguardian.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNotificationEventsUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    operator fun invoke(): Flow<List<NotificationEvent>> =
        repository.getNotificationEvents()
}
