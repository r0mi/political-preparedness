package com.example.android.politicalpreparedness.data.source.local

import androidx.lifecycle.LiveData
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.network.models.Election
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DefaultElectionsLocalDataSource internal constructor(
    private val electionDao: ElectionDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ElectionsLocalDataSource {
    override fun observeFollowedElections(): LiveData<List<Election>> {
        return electionDao.observeElections()
    }

    override suspend fun followElection(election: Election) = withContext(ioDispatcher) {
        electionDao.insertElection(election)
    }

    override suspend fun unFollowElection(election: Election): Unit = withContext(ioDispatcher) {
        electionDao.deleteElectionById(election.id)
    }

    override suspend fun isElectionFollowed(electionId: Int): Boolean = withContext(ioDispatcher) {
        electionDao.getElectionById(electionId) != null
    }
}