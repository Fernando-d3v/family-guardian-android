package com.familyguardian.core.utils

object Constants {

    /**
     * Base URL is defined per build type in app/build.gradle.kts via BuildConfig.BASE_URL:
     *   debug   → http://10.0.2.2:8000/  (Android emulator → localhost Laravel)
     *   release → https://api.familyguardian.com/
     */

    /** Room database file name. */
    const val DATABASE_NAME = "family_guardian_db"

    /**
     * Fully-qualified class name of the NotificationListenerService.
     * Used to check if the service is enabled without a direct import dependency.
     */
    const val NOTIFICATION_SERVICE_CLASS =
        "com.familyguardian.service.NotificationMonitorService"

    /** Apps whose notifications will be captured and analysed. */
    val MONITORED_PACKAGES: Set<String> = setOf(
        "com.whatsapp",            // WhatsApp personal
        "com.whatsapp.w4b",        // WhatsApp Business
        "com.instagram.android",   // Instagram DMs
    )

    /** Maximum number of recent notification events kept in the local database. */
    const val MAX_STORED_EVENTS = 100
}
