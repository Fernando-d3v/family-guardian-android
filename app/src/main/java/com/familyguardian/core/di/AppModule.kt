package com.familyguardian.core.di

import android.content.Context
import androidx.room.Room
import com.familyguardian.core.utils.Constants
import com.familyguardian.data.local.dao.NotificationDao
import com.familyguardian.data.local.database.AppDatabase
import com.familyguardian.data.repository.NotificationRepositoryImpl
import com.familyguardian.domain.repository.NotificationRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            Constants.DATABASE_NAME
        )
            // For development: drop and recreate on schema change.
            // Replace with proper migrations before shipping to production.
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideNotificationDao(database: AppDatabase): NotificationDao =
        database.notificationDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        impl: NotificationRepositoryImpl
    ): NotificationRepository
}
