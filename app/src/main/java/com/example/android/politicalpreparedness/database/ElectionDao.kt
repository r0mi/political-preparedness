package com.example.android.politicalpreparedness.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.android.politicalpreparedness.network.models.Election

@Dao
interface ElectionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertElection(election: Election)

    @Query("SELECT * FROM election_table ORDER BY electionDay ASC")
    suspend fun getElections(): List<Election>

    @Query("SELECT * FROM election_table ORDER BY electionDay ASC")
    fun observeElections(): LiveData<List<Election>>

    @Query("SELECT * FROM election_table WHERE id = :electionId")
    suspend fun getElectionById(electionId: Int): Election?

    @Query("DELETE FROM election_table WHERE id = :electionId")
    suspend fun deleteElectionById(electionId: Int): Int

    @Query("DELETE FROM election_table")
    suspend fun deleteElections()

}