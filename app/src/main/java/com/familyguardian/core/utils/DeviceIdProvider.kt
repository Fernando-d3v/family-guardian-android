package com.familyguardian.core.utils

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides a stable, anonymous device identifier that persists across app restarts.
 *
 * A random UUID is generated on first run and stored in private SharedPreferences.
 * This avoids the privacy concerns of [android.provider.Settings.Secure.ANDROID_ID]
 * (which can change on factory reset and is tied to the device, not the install).
 */
@Singleton
class DeviceIdProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getDeviceId(): String =
        prefs.getString(KEY_DEVICE_ID, null) ?: generateAndStore()

    private fun generateAndStore(): String {
        val id = UUID.randomUUID().toString()
        prefs.edit().putString(KEY_DEVICE_ID, id).apply()
        return id
    }

    private companion object {
        const val PREFS_NAME = "fg_device_prefs"
        const val KEY_DEVICE_ID = "device_id"
    }
}
