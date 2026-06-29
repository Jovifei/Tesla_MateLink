package com.teslamatelink

import android.app.Application
import com.tencent.mmkv.MMKV
import dagger.hilt.android.HiltAndroidApp

/**
 * Tesla MateLink entry point.
 *
 * Initializes MMKV for performant key-value storage and
 * Hilt for dependency injection across the app.
 */
@HiltAndroidApp
class MateLinkApp : Application() {

    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
    }
}
