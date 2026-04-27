package com.familyguardian.data.remote.api

import com.familyguardian.data.remote.dto.NotificationEventDto
import retrofit2.http.Body
import retrofit2.http.POST

interface FamilyGuardianApi {

    /**
     * Sends a single captured notification event to the Laravel backend.
     * Retrofit throws [retrofit2.HttpException] on 4xx/5xx — caught by the repository.
     */
    @POST("api/events")
    suspend fun sendEvent(@Body event: NotificationEventDto)
}
