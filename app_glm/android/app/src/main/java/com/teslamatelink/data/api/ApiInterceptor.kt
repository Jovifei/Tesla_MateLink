package com.teslamatelink.data.api

import com.tencent.mmkv.MMKV
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp interceptor that attaches a Bearer token to every request.
 *
 * The token is read from MMKV so it can be updated at runtime
 * without rebuilding the OkHttpClient.
 */
@Singleton
class ApiInterceptor @Inject constructor() : Interceptor {

    companion object {
        private const val TAG = "ApiInterceptor"
        private const val KEY_AUTH_TOKEN = "auth_token"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = MMKV.defaultMMKV().decodeString(KEY_AUTH_TOKEN, "")

        val request = if (token.isNotBlank()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                // TeslaMate API expects application/json
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build()
        } else {
            originalRequest.newBuilder()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build()
        }

        return chain.proceed(request)
    }
}
