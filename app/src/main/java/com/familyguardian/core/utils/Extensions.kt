package com.familyguardian.core.utils

import android.content.Context
import androidx.core.app.NotificationManagerCompat

/**
 * Returns true if this app's NotificationListenerService is currently
 * granted access by the user via Settings > Special app access > Notification access.
 */
fun Context.isNotificationListenerEnabled(): Boolean =
    NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)
