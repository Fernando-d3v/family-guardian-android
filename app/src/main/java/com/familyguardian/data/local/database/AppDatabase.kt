package com.familyguardian.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.familyguardian.data.local.dao.NotificationDao
import com.familyguardian.data.local.entity.NotificationEntity

@Database(
    entities = [NotificationEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
}
