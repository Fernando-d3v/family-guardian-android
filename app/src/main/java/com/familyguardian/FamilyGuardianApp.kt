package com.familyguardian

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.familyguardian.BuildConfig
import com.familyguardian.data.worker.SendPendingEventsWorker
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class FamilyGuardianApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    /**
     * Custom WorkManager configuration that uses Hilt's worker factory so that
     * @HiltWorker workers receive their injected dependencies.
     *
     * Because we implement [Configuration.Provider], the default WorkManager
     * content provider initialisation is disabled (see AndroidManifest.xml).
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        scheduleBackgroundWork()
    }

    private fun scheduleBackgroundWork() {
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            SendPendingEventsWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,   // don't reset timer if already scheduled
            SendPendingEventsWorker.buildPeriodicRequest()
        )
    }
}
