package com.familyguardian.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_events")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val bigText: String,
    val source: String,
    val timestamp: Long,
    /** False until the event has been successfully delivered to the backend API. */
    val isSent: Boolean
)
