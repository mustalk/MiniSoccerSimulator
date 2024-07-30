package com.mustalk.minisimulator

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.mustalk.minisimulator.BuildConfig.DEBUG
import dagger.hilt.android.HiltAndroidApp

/**
 * @author by MusTalK on 15/07/2024
 */

@HiltAndroidApp
class MiniSimulatorApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase for non-debug builds
        if (!DEBUG) {
            FirebaseApp.initializeApp(this)
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        }
    }
}
