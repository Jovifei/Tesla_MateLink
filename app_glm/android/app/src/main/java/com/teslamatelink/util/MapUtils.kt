package com.teslamatelink.util

import android.content.Context
import java.util.Locale

object MapUtils {
    fun isChineseLocale(): Boolean {
        return Locale.getDefault().language.startsWith("zh")
    }

    /**
     * Checks whether a non-empty AMap API key is configured in AndroidManifest.xml.
     */
    fun isAmapKeyConfigured(context: Context): Boolean {
        return try {
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName, android.content.pm.PackageManager.GET_META_DATA
            )
            val key = appInfo.metaData?.getString("com.amap.api.v2.apikey")
            !key.isNullOrBlank() && key != "YOUR_AMAP_API_KEY"
        } catch (_: Exception) {
            false
        }
    }
}
