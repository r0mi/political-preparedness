package com.example.android.politicalpreparedness.data.source.remote

import com.example.android.politicalpreparedness.data.Result
import com.example.android.politicalpreparedness.network.CivicsApiService
import com.example.android.politicalpreparedness.network.models.Election
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DefaultRemoteDataSource(
    private val apiService: CivicsApiService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : RemoteDataSource {
    override suspend fun getElections(): Result<List<Election>> = withContext(ioDispatcher) {
        return@withContext try {
            val result = apiService.getElections()
            Result.Success(result.elections)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}