package com.familyguardian.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.familyguardian.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: NotificationEntity)

    /** Returns the 100 most recent captured events, newest first. */
    @Query("SELECT * FROM notification_events ORDER BY timestamp DESC LIMIT 100")
    fun getRecentEvents(): Flow<List<NotificationEntity>>

    /** Returns up to 50 events not yet delivered to the API, oldest first (for retry). */
    @Query("SELECT * FROM notification_events WHERE isSent = 0 ORDER BY timestamp ASC LIMIT 50")
    suspend fun getUnsentEvents(): List<NotificationEntity>

    /** Marks a single event as successfully delivered. */
    @Query("UPDATE notification_events SET isSent = 1 WHERE id = :id")
    suspend fun markAsSent(id: String)

    /** Purge old records to keep database size bounded. */
    @Query("DELETE FROM notification_events WHERE timestamp < :cutoffTimestamp")
    suspend fun deleteOlderThan(cutoffTimestamp: Long)

    @Query("DELETE FROM notification_events")
    suspend fun deleteAll()
}
