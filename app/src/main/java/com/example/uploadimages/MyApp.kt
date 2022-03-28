package com.example.uploadimages

import android.app.Application
import com.google.firebase.FirebaseApp
import timber.log.Timber

/**
 * Created by Franz Andel <franz.andel@ovo.id>
 * on 10 March 2022.
 */

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        FirebaseApp.initializeApp(this)
    }
}
