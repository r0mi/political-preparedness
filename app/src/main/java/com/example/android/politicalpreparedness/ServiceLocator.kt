package com.example.android.politicalpreparedness

import android.content.Context
import com.example.android.politicalpreparedness.data.source.DefaultElectionsRepository
import com.example.android.politicalpreparedness.data.source.ElectionsRepository
import com.example.android.politicalpreparedness.data.source.local.ElectionsLocalDataSource
import com.example.android.politicalpreparedness.data.source.remote.DefaultRemoteDataSource
import com.example.android.politicalpreparedness.data.source.remote.RemoteDataSource
import com.example.android.politicalpreparedness.network.CivicsApi

object ServiceLocator {
    @Volatile
    var electionsRepository: ElectionsRepository? = null

    fun provideElectionsRepository(context: Context): ElectionsRepository {
        synchronized(this) {
            return electionsRepository ?: createElectionsRepository(context)
        }
    }

    private fun createElectionsRepository(context: Context): ElectionsRepository {
        return DefaultElectionsRepository(
            createRemoteDataSource(),
            createElectionsLocalDataSource(context)
        ).apply {
            electionsRepository = this
        }
    }

    private fun createElectionsLocalDataSource(context: Context): ElectionsLocalDataSource {
        return object : ElectionsLocalDataSource {

        }
    }

    private fun createRemoteDataSource(): RemoteDataSource {
        return DefaultRemoteDataSource(CivicsApi.retrofitService)
    }
}