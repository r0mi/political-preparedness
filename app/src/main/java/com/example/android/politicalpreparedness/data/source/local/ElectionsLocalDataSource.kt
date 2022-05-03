package com.example.android.politicalpreparedness.data.source.local

import androidx.lifecycle.LiveData
import com.example.android.politicalpreparedness.network.models.Election

interface ElectionsLocalDataSource {
    fun observeFollowedElections(): LiveData<List<Election>>
    suspend fun followElection(election: Election)
    suspend fun unFollowElection(election: Election)
    suspend fun isElectionFollowed(electionId: Int): Boolean
}