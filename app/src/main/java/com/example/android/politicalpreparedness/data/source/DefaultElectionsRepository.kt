package com.example.android.politicalpreparedness.data.source

import com.example.android.politicalpreparedness.data.Result
import com.example.android.politicalpreparedness.data.source.local.ElectionsLocalDataSource
import com.example.android.politicalpreparedness.data.source.remote.RemoteDataSource
import com.example.android.politicalpreparedness.network.models.Election
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DefaultElectionsRepository(
    private val remoteDataSource: RemoteDataSource,
    private val electionsLocalDataSource: ElectionsLocalDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ElectionsRepository {
    override suspend fun getElections(): Result<List<Election>> = withContext(ioDispatcher) {
        return@withContext remoteDataSource.getElections()
    }
}
