package com.example.android.politicalpreparedness.data.source

import androidx.lifecycle.LiveData
import com.example.android.politicalpreparedness.data.Result
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse

interface ElectionsRepository {
    suspend fun getElections(): Result<List<Election>>
    suspend fun getVoterInfo(address: String, electionId: Int): Result<VoterInfoResponse>

    fun observeFollowedElections(): LiveData<List<Election>>
    suspend fun followElection(election: Election)
    suspend fun unFollowElection(election: Election)
    suspend fun isElectionFollowed(electionId: Int): Boolean

}