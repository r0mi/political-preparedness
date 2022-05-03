package com.example.android.politicalpreparedness.data.source

import androidx.lifecycle.LiveData
import com.example.android.politicalpreparedness.data.Result
import com.example.android.politicalpreparedness.data.source.local.ElectionsLocalDataSource
import com.example.android.politicalpreparedness.data.source.remote.RemoteDataSource
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.RepresentativeResponse
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
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

    override suspend fun getVoterInfo(address: String, electionId: Int): Result<VoterInfoResponse> =
        withContext(ioDispatcher) {
            return@withContext remoteDataSource.getVoterInfo(address, electionId)
        }

    override suspend fun getRepresentatives(address: String): Result<RepresentativeResponse> =
        withContext(ioDispatcher) {
            return@withContext remoteDataSource.getRepresentatives(address)
        }

    override fun observeFollowedElections(): LiveData<List<Election>> {
        return electionsLocalDataSource.observeFollowedElections()
    }

    override suspend fun followElection(election: Election) = withContext(ioDispatcher) {
        electionsLocalDataSource.followElection(election)
    }

    override suspend fun unFollowElection(election: Election) = withContext(ioDispatcher) {
        electionsLocalDataSource.unFollowElection(election)
    }

    override suspend fun isElectionFollowed(electionId: Int): Boolean = withContext(ioDispatcher) {
        return@withContext electionsLocalDataSource.isElectionFollowed(electionId)
    }
}
