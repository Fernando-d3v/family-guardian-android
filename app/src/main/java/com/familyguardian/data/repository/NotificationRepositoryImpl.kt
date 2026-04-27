package com.familyguardian.data.repository

import com.familyguardian.core.utils.DeviceIdProvider
import com.familyguardian.core.utils.NotificationEventBus
import com.familyguardian.data.local.dao.NotificationDao
import com.familyguardian.data.mapper.toDomain
import com.familyguardian.data.mapper.toDto
import com.familyguardian.data.mapper.toEntity
import com.familyguardian.data.remote.api.FamilyGuardianApi
import com.familyguardian.domain.model.NotificationEvent
import com.familyguardian.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val dao: NotificationDao,
    private val eventBus: NotificationEventBus,
    private val api: FamilyGuardianApi,
    private val deviceIdProvider: DeviceIdProvider
) : NotificationRepository {

    override fun getNotificationEvents(): Flow<List<NotificationEvent>> =
        dao.getRecentEvents().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun saveNotificationEvent(event: NotificationEvent) {
        Timber.d("Saving event locally: [${event.appName}] ${event.title}")
        dao.insert(event.toEntity())   // stored with isSent=false
        eventBus.tryEmit(event)
    }

    override suspend fun sendEventToApi(event: NotificationEvent): Result<Unit> =
        runCatching {
            val dto = event.toDto(deviceIdProvider.getDeviceId())
            api.sendEvent(dto)
            dao.markAsSent(event.id)
            Timber.d("Event sent to API: id=${event.id} app=${event.appName}")
        }.onFailure { error ->
            Timber.w(error, "API send failed for event ${event.id} — will be retried by WorkManager")
        }

    override suspend fun retryFailedEvents() {
        val unsent = dao.getUnsentEvents()
        if (unsent.isEmpty()) return

        Timber.d("Retrying ${unsent.size} unsent event(s)")
        unsent.forEach { entity ->
            sendEventToApi(entity.toDomain())
        }
    }

    override fun observeNewEvents(): Flow<NotificationEvent> =
        eventBus.events
}
