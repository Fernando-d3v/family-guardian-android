package com.familyguardian.core.utils

import com.familyguardian.domain.model.NotificationEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-process event bus that bridges the [NotificationMonitorService]
 * (which runs outside the normal ViewModel scope) with the repository layer.
 *
 * Using a [MutableSharedFlow] with a replay buffer of 0 ensures that only
 * active collectors receive events — no stale data leaks across screen rotations.
 */
@Singleton
class NotificationEventBus @Inject constructor() {

    private val _events = MutableSharedFlow<NotificationEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<NotificationEvent> = _events.asSharedFlow()

    /**
     * Non-suspending emit — safe to call from a service's coroutine scope.
     * Returns false only when the buffer is full (64 unprocessed events).
     */
    fun tryEmit(event: NotificationEvent): Boolean = _events.tryEmit(event)
}
