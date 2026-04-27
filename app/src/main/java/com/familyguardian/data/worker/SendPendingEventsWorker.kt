package com.familyguardian.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import com.familyguardian.domain.usecase.RetryFailedEventsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Periodic background job that retries locally stored events that failed to reach the API.
 *
 * Runs every 15 minutes (minimum WorkManager interval) only when a network connection
 * is available. Uses exponential back-off on repeated failures.
 *
 * WorkManager initialisation is handled by [com.familyguardian.FamilyGuardianApp]
 * which implements [androidx.work.Configuration.Provider] with Hilt's worker factory.
 */
@HiltWorker
class SendPendingEventsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val retryFailedEventsUseCase: RetryFailedEventsUseCase
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Timber.d("SendPendingEventsWorker: starting retry pass")
        return runCatching {
            retryFailedEventsUseCase()
        }.fold(
            onSuccess = {
                Timber.d("SendPendingEventsWorker: completed successfully")
                Result.success()
            },
            onFailure = { error ->
                Timber.e(error, "SendPendingEventsWorker: failed, scheduling retry")
                Result.retry()
            }
        )
    }

    companion object {
        const val WORK_NAME = "send_pending_notification_events"

        fun buildPeriodicRequest(): PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<SendPendingEventsWorker>(
                repeatInterval = 15,
                repeatIntervalTimeUnit = TimeUnit.MINUTES
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    30,
                    TimeUnit.SECONDS
                )
                .build()
    }
}
