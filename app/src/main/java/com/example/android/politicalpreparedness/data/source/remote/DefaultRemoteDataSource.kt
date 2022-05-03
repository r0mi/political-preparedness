package com.example.android.politicalpreparedness.data.source.remote

import com.example.android.politicalpreparedness.data.Result
import com.example.android.politicalpreparedness.network.CivicsApiService
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.RepresentativeResponse
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
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

    override suspend fun getVoterInfo(address: String, electionId: Int): Result<VoterInfoResponse> =
        withContext(ioDispatcher) {
            return@withContext try {
                Result.Success(apiService.getVoterInfo(address, electionId))
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

    override suspend fun getRepresentatives(address: String): Result<RepresentativeResponse> =
        withContext(ioDispatcher) {
            return@withContext try {
                Result.Success(apiService.getRepresentatives(address))
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
}