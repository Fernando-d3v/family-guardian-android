package com.familyguardian.data.remote.interceptor

import com.familyguardian.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Adds authentication and common headers to every outgoing request.
 *
 * Token is read from [BuildConfig.API_TOKEN] which is set per build type in
 * app/build.gradle.kts. Replace with a proper token store when device
 * registration / login is implemented.
 */
@Singleton
class AuthInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")

        val token = BuildConfig.API_TOKEN
        if (token.isNotBlank()) {
            builder.header("Authorization", "Bearer $token")
        }

        return chain.proceed(builder.build())
    }
}
