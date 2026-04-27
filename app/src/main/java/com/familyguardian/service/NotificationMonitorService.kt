package com.familyguardian.service

import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.familyguardian.core.utils.Constants
import com.familyguardian.domain.model.NotificationEvent
import com.familyguardian.domain.usecase.SaveNotificationEventUseCase
import com.familyguardian.domain.usecase.SendNotificationEventUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

/**
 * System service that receives callbacks whenever a notification is posted or removed.
 *
 * Requires the user to manually grant access via:
 *   Settings > Apps > Special app access > Notification access
 *
 * Flow per captured event:
 *  1. [saveNotificationEventUseCase] → persists to Room with isSent=false
 *  2. [sendNotificationEventUseCase] → sends to API; marks isSent=true on success
 *  3. If API send fails → WorkManager's [SendPendingEventsWorker] retries later
 *
 * The [SupervisorJob] ensures that a failure in one coroutine job doesn't
 * cancel sibling jobs processing other notifications.
 */
@AndroidEntryPoint
class NotificationMonitorService : NotificationListenerService() {

    @Inject
    lateinit var saveNotificationEventUseCase: SaveNotificationEventUseCase

    @Inject
    lateinit var sendNotificationEventUseCase: SendNotificationEventUseCase

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onListenerConnected() {
        super.onListenerConnected()
        Timber.i("NotificationMonitorService connected — monitoring active")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Timber.w("NotificationMonitorService disconnected")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    // ── Notification callbacks ────────────────────────────────────────────────

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return

        val packageName = sbn.packageName ?: return
        if (packageName !in Constants.MONITORED_PACKAGES) return

        val extras = sbn.notification?.extras ?: return

        val title = extras.getCharSequence("android.title")?.toString().orEmpty().trim()
        val text = extras.getCharSequence("android.text")?.toString().orEmpty().trim()
        // BigText contains the full expanded message; may equal text when not set
        val bigText = extras.getCharSequence("android.bigText")?.toString().orEmpty().trim()

        // Skip empty or purely-ticker notifications (e.g. "Typing…" with no body)
        if (title.isBlank() && text.isBlank()) return

        val event = NotificationEvent(
            id = UUID.randomUUID().toString(),
            packageName = packageName,
            appName = resolveAppName(packageName),
            title = title,
            text = text,
            bigText = bigText,
            source = "notification",
            timestamp = sbn.postTime
        )

        Timber.d("Captured ← [${event.appName}] ${event.title}: ${event.text}")

        serviceScope.launch {
            runCatching { saveNotificationEventUseCase(event) }
                .onSuccess {
                    // Fire-and-forget: failures are logged and retried by WorkManager
                    sendNotificationEventUseCase(event)
                        .onFailure { Timber.w(it, "Immediate API send failed — queued for retry") }
                }
                .onFailure { Timber.e(it, "Failed to persist notification event") }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // Reserved for future use (e.g. deleted-message detection)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun resolveAppName(packageName: String): String =
        runCatching {
            val pm = applicationContext.packageManager
            pm.getApplicationLabel(
                pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            ).toString()
        }.getOrDefault(packageName)
}
