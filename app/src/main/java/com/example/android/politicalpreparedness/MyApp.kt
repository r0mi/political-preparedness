package com.example.android.politicalpreparedness

import android.app.Application
import com.example.android.politicalpreparedness.data.source.ElectionsRepository
import timber.log.Timber

class MyApp : Application() {

    val electionsRepository: ElectionsRepository
        get() = ServiceLocator.provideElectionsRepository(this)

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}